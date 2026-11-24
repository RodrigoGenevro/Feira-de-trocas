# Theme briefing: Swap and donate fair

**English** · [Português (Brasil)](BRIEFING.pt-BR.md)

**Hack with AI: Coding with Intelligence**  
Senac na Prática · Technical Program in Systems Development  
Area: campus life and community  
Immersion from 23 to 29 November · presentations on 30 November and 1 December

This briefing is the starting point for **your** theme. Read it carefully, cut an MVP that fits in one week, and record your decisions. You will turn this problem into a real product, with a front end and a back end, an organized repository, and documented use of AI. This is a collaborative immersion: the panel evaluates what the team built, decided, and learned.

## Theme and context

**Swap and donate fair** — A marketplace to swap or donate used books, materials, and equipment.

Books, kits, calculators, cables, and even a perfectly good mouse move in and out of a student's life all the time. When the course ends, the material sits on a shelf — while someone in the next class needs the same item and does not know whom to ask. Swaps and donations already happen in hallways and on social media, but unevenly: the listing disappears, the arrangement is fragile, and it is not always clear whether the item is available, reserved, or already handed over.

You will create a trustworthy board for the campus community to swap or donate what still has use, with clear listings and a simple flow through to the meetup.

## Audience and personas

- **Someone who wants to donate or swap:** needs to describe the item easily and mark it when it is no longer available.
- **Someone looking for a material:** wants to search by category or keyword and show interest without noise.
- **People in the campus community:** the scope is Senac and student life around it, not a shop open to any buyer.

## Goal

Ship an MVP of a swap-and-donate board where someone can publish an item, find it in search, show interest, and close the arrangement — with AI helping to describe and organize the listings.

## Minimum requirements (MVP)

The scope below is realistic for one week. Lock it on the first day and record in `DECISIONS.md` what is in, what is out, and why. A complete, demonstrable flow is better than a long list left half-done.

1. Publish a listing with a title, description, photo, category, and mode (swap or donation).
2. List, search, and filter listings by category or keyword.
3. Show interest in an item (a request or short message recorded in the system).
4. Update the listing status (available, reserved, completed).
5. A simple profile for the person who lists (a name and, if it makes sense, a class), without unnecessary data.
6. The person who listed the item can see incoming interest and accept it or close the listing.

## Where AI comes in

The use of AI is allowed and encouraged — in the product and in the process — and it must appear in `AI_LOG.md`. Some concrete starting points for this theme:

- Generate or improve the description from the photo and a few short notes, for the person to review.
- Suggest a category and a condition (for example, used, in good condition).
- Help with search in natural language (“looking for a database book”).
- Flag unclear listings so the publisher can complete the information before finishing.

Do not publish, without your review, an AI-generated text or automatic decision as if it were already the final version.

## Optional extras

Only after the MVP is standing. These are ideas, not obligations:

- A suggested meetup point on campus.
- A favorites list.
- A “I’m looking for” board in addition to items already listed.
- A notice when an item similar to what the person wants is published.

## Out of scope

To protect the deadline and the focus of the product:

- Payment, checkout, or a shopping cart — the scope is swapping or donating, not running a shop.
- Shipping or home-delivery logistics.
- Invoices, warranties, and returns as in online retail.
- Listings open to general commerce, outside the campus community.

## Questions to guide the team

Use these questions on the first day, when you define the MVP, and again before the demo:

- How do you handle an inappropriate listing or an item that should not be there?
- What happens if an item is reserved and nobody picks it up?
- How do the two people arrange a safe meetup without putting a phone number on a public page?
- How does search behave when the description is short or the photo is poor?
- Who can edit or take down a listing, besides the person who created it?

## Deliverables and how the panel evaluates

This is a one-week **collaborative immersion**, with teams of 3 or 4 TDS students. The panel evaluates **your** work, on your own project — formative feedback, not a comparison with other teams.

**What must be in the repository** (at the root), in **English** (the main version) and in **Portuguese** (`.pt-BR`), with a language link at the top of each file:

- `README.md` and `README.pt-BR.md`
- `DECISIONS.md` and `DECISIONS.pt-BR.md`
- `AI_LOG.md` and `AI_LOG.pt-BR.md`

The application must have a **front end and a back end**. The back end may be **Java**, **Python**, or **Node.js** (alone or combined, as long as they are integrated). A front end is required (web, desktop, or mobile). Justify the stack in `DECISIONS.md`. Also required: a Conventional Commits history, work through reviewed Pull Requests, a protected `main` branch, and a green **GitHub Actions** pipeline (build, lint, and tests) on PRs and on `main`. Do not commit secrets. On the first day, add the instructor as a collaborator: **adrianoanschau@gmail.com**.

**Code freeze** is on Sunday, **29 November, at 23:59**. Presentations are **15 minutes + 5 minutes of questions**, on 30 November and 1 December, at Senac Canoas, Room 103. Rehearse the demo and the required outline (problem and solution, live demo, workflow, decisions, AI in practice, and CI/CD).

Evaluation is split equally between **two panels**:

- **Technical panel (50%):** code quality and architecture; Git, Conventional Commits, and PRs; CI/CD; command of the code in the Q&A.
- **Innovation and Impact panel (50%):** originality; impact (the real problem and for whom); user experience; clarity of the pitch; creative use of AI.

Anyone on the team may be asked about any part of the project. Use AI thoughtfully, record what it generated and what you corrected, and own the code you present.

Good work — and may the week be intense, collaborative, and well documented.

