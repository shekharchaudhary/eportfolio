ePortfolio — GitHub Pages
=========================

This repository hosts the ePortfolio site for Shekhar Chaudhary.

Live URL
--------

- Project site: `https://shekharchaudhary.github.io/eportfolio`

Structure
---------

- `docs/` — Static site (HTML/CSS/JS). GitHub Pages will publish this.
- `.github/workflows/pages.yml` — GitHub Actions workflow that deploys `docs/` to Pages.

Local Edits
-----------

1. Update content in `docs/index.html` (name, email, projects, experience, etc.).
2. Add your resume to `docs/resume.pdf` and update the link in `index.html`.
3. Adjust styles in `docs/styles.css` as needed.
4. Add Module One document:
   - Preferred: export your DOCX to PDF and place it at `docs/artifacts/CS499_Module_One.pdf`.
   - Optional: also place the original at `docs/artifacts/CS499_Module_One.docx`.
   - The page `docs/artifacts/module-one.html` will embed the PDF automatically.

Converting DOCX to PDF/HTML
---------------------------

- Microsoft Word: File → Save As → PDF.
- macOS (Terminal): `textutil -convert pdf \
  -output docs/artifacts/CS499_Module_One.pdf \
  /path/to/CS499_Module_One.docx`
- Pandoc (if installed): `pandoc CS499_Module_One.docx -o docs/artifacts/CS499_Module_One.pdf`

Publish with GitHub Pages
-------------------------

Option A — GitHub Actions (recommended):

1. In GitHub: Settings → Pages → Build and deployment → Source: select “GitHub Actions”.
2. Push to `main`. The `pages.yml` workflow uploads `docs/` and deploys.
3. Wait for the green “Your site is published” notice, then visit the URL above.

Option B — Deploy from a branch:

1. In GitHub: Settings → Pages → Build and deployment → Source: “Deploy from a branch”.
2. Branch: `main`, Folder: `/docs`.
3. Save. Wait 1–2 minutes and visit the URL.

Notes
-----

- `docs/.nojekyll` disables Jekyll processing so folders with underscores won’t break.
- `docs/404.html` provides a nicer “not found” page and helps SPA routing if needed.
- For a custom domain, add `docs/CNAME` containing only your domain and set DNS (CNAME to `shekharchaudhary.github.io`).
