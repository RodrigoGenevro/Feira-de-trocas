package br.com.unimart;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class Main {
    private static final Gson gson = new Gson();
    private static final String DB = "jdbc:sqlite:../database/feira_trocas.db";
    private static final Path PUBLIC = Path.of("../frontend");

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("../database"));
        initDatabase();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/anuncios", Main::anuncios);
        server.createContext("/api/interesses", Main::interesses);
        server.createContext("/", Main::staticFiles);
        server.setExecutor(null);
        server.start();
        System.out.println("UniMart rodando em http://localhost:8080");
    }

    private static Connection connect() throws SQLException { return DriverManager.getConnection(DB); }
    private static void initDatabase() throws SQLException {
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS anuncios (id INTEGER PRIMARY KEY AUTOINCREMENT, titulo TEXT NOT NULL, descricao TEXT NOT NULL, categoria TEXT NOT NULL, modalidade TEXT NOT NULL, nome TEXT NOT NULL, turma TEXT, foto TEXT, status TEXT NOT NULL DEFAULT 'DISPONIVEL', criado_em TEXT DEFAULT CURRENT_TIMESTAMP)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS interesses (id INTEGER PRIMARY KEY AUTOINCREMENT, anuncio_id INTEGER NOT NULL, nome TEXT NOT NULL, mensagem TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'PENDENTE', criado_em TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(anuncio_id) REFERENCES anuncios(id))");
        }
    }
    private static void anuncios(HttpExchange ex) throws IOException {
        try {
            String method=ex.getRequestMethod();
            if(method.equals("GET")) {
                List<Map<String,Object>> rows=new ArrayList<>();
                String q=param(ex.getRequestURI().getRawQuery(),"q"), cat=param(ex.getRequestURI().getRawQuery(),"categoria");
                try(Connection c=connect(); PreparedStatement p=c.prepareStatement("SELECT * FROM anuncios WHERE (?='' OR lower(titulo || ' ' || descricao) LIKE ?) AND (?='' OR categoria=?) ORDER BY id DESC")) {
                    p.setString(1,q); p.setString(2,"%"+q.toLowerCase()+"%"); p.setString(3,cat); p.setString(4,cat);
                    try(ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object> m=new LinkedHashMap<>(); for(String k:List.of("id","titulo","descricao","categoria","modalidade","nome","turma","foto","status","criado_em"))m.put(k,r.getObject(k)); rows.add(m);}}
                }
                send(ex,200,gson.toJson(rows)); return;
            }
            if(method.equals("POST")) {
                JsonObject j=body(ex); try(Connection c=connect();PreparedStatement p=c.prepareStatement("INSERT INTO anuncios(titulo,descricao,categoria,modalidade,nome,turma,foto) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1, val(j,"titulo"));p.setString(2,val(j,"descricao"));p.setString(3,val(j,"categoria"));p.setString(4,val(j,"modalidade"));p.setString(5,val(j,"nome"));p.setString(6,val(j,"turma"));p.setString(7,val(j,"foto"));
                    if(p.executeUpdate()==0)throw new SQLException("Não foi possível salvar"); try(ResultSet k=p.getGeneratedKeys()){k.next();send(ex,201,"{\"id\":"+k.getInt(1)+"}");}
                } return;
            }
            if(method.equals("PATCH")) {
                String[] path=ex.getRequestURI().getPath().split("/"); int id=Integer.parseInt(path[path.length-1]); String status=val(body(ex),"status");
                try(Connection c=connect();PreparedStatement p=c.prepareStatement("UPDATE anuncios SET status=? WHERE id=?")){p.setString(1,status);p.setInt(2,id);p.executeUpdate();}
                send(ex,200,"{\"ok\":true}"); return;
            }
            send(ex,405,"{\"erro\":\"Método não permitido\"}");
        } catch(Exception e){send(ex,400,gson.toJson(Map.of("erro",Objects.toString(e.getMessage(),"Erro"))));}
    }
    private static void interesses(HttpExchange ex) throws IOException {
        try {
            if(ex.getRequestMethod().equals("POST")) { JsonObject j=body(ex); try(Connection c=connect();PreparedStatement p=c.prepareStatement("INSERT INTO interesses(anuncio_id,nome,mensagem) VALUES(?,?,?)")){p.setInt(1,j.get("anuncio_id").getAsInt());p.setString(2,val(j,"nome"));p.setString(3,val(j,"mensagem"));p.executeUpdate();} send(ex,201,"{\"ok\":true}"); return; }
            if(ex.getRequestMethod().equals("GET")){List<Map<String,Object>> a=new ArrayList<>();try(Connection c=connect();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT i.*,a.titulo FROM interesses i JOIN anuncios a ON a.id=i.anuncio_id ORDER BY i.id DESC")){while(r.next()){Map<String,Object> m=new LinkedHashMap<>();for(String k:List.of("id","anuncio_id","nome","mensagem","status","titulo"))m.put(k,r.getObject(k));a.add(m);}}send(ex,200,gson.toJson(a));return;}
            send(ex,405,"{}");
        }catch(Exception e){send(ex,400,gson.toJson(Map.of("erro",Objects.toString(e.getMessage(),"Erro"))));}
    }
    private static void staticFiles(HttpExchange ex) throws IOException {
        String path=ex.getRequestURI().getPath(); if(path.equals("/"))path="/index.html";
        Path file=PUBLIC.resolve(path.substring(1)).normalize();
        if(!file.startsWith(PUBLIC)||!Files.exists(file)){send(ex,404,"Página não encontrada");return;}
        String type=file.toString().endsWith(".css")?"text/css; charset=utf-8":file.toString().endsWith(".js")?"application/javascript; charset=utf-8":"text/html; charset=utf-8";
        byte[] data=Files.readAllBytes(file);ex.getResponseHeaders().set("Content-Type",type);ex.sendResponseHeaders(200,data.length);try(OutputStream o=ex.getResponseBody()){o.write(data);}
    }
    private static JsonObject body(HttpExchange ex)throws IOException{return gson.fromJson(new String(ex.getRequestBody().readAllBytes(),StandardCharsets.UTF_8),JsonObject.class);}
    private static String val(JsonObject j,String k){return j.has(k)&&!j.get(k).isJsonNull()?j.get(k).getAsString().trim():"";}
    private static String param(String query,String key){if(query==null)return "";for(String p:query.split("&")){String[] kv=p.split("=",2);if(kv[0].equals(key))return URLDecoder.decode(kv.length>1?kv[1]:"",StandardCharsets.UTF_8);}return "";}
    private static void send(HttpExchange ex,int code,String text)throws IOException{byte[] b=text.getBytes(StandardCharsets.UTF_8);ex.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");ex.getResponseHeaders().set("Access-Control-Allow-Origin","*");ex.sendResponseHeaders(code,b.length);try(OutputStream o=ex.getResponseBody()){o.write(b);}}
}
