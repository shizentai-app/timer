# Changelog

Append-only log of meaningful changes to Shizentai Timer. The audience is **future you / a fresh Claude session** — entries should be skimmable in 30 seconds and detailed enough to avoid repeating debugging journeys.

## File format

```
NNN-YYYY-MM-DD-kebab-case-slug.md
```

- `NNN` — three-digit sequence (`001`, `002`, …). Never re-use a number, never reorder.
- `YYYY-MM-DD` — the date the change landed (or the date the work began for multi-day items).
- `kebab-case-slug` — short; one to four words; describes the *change*, not the symptom.

Example: `007-2026-05-07-language-picker-config-changes.md`

## Entry shape

Keep each entry focused on **one** change. If two unrelated things happened the same day, write two entries.

```markdown
# {Title}

**Date:** YYYY-MM-DD · **Files:** {short list of touched paths or globs}

## Why
One paragraph: what problem this solves, what user feedback or insight prompted it.

## What changed
Bullet points. Be concrete — name files, name decisions. No marketing copy.

## How it was decided
If the approach has alternatives that were tried and rejected, list them with
the reason. This is the most valuable part for the next person — saves them
re-walking the same dead ends.

## Known follow-ups
What's left undone, what to watch in production, what to revisit.
```

The "How it was decided" section is the changelog's actual value — anyone can read a git diff for *what* changed, but the *why-not-the-other-way* dies in the conversation otherwise.

## When to write one

- Architectural decisions (engine shape, persistence model, navigation)
- Visible UX changes the user would notice on a screenshot
- Build / toolchain / dependency changes
- Bugs that took more than one attempt to fix (the rejected fixes are the lesson)
- Anything you'd want to remember in 6 months

**Do not** write entries for:
- Renames, formatting, comment edits
- Pure dependency version bumps (unless they fixed a behavior)
- Work-in-progress; wait until it lands

## What never goes in a changelog

These repos are public. Every entry must read fine to a stranger on GitHub.

- **No absolute machine paths** containing usernames (`C:\Users\<name>\…`, `/Users/<name>/…`, `/home/<name>/…`). Use repo-relative paths (`android/app/src/main/kotlin/...`).
- **No personal names, emails, phone numbers, addresses.** Refer to people by role: "the reviewer", "the owner", "a tester".
- **No API keys, OAuth secrets, deploy tokens, signing certs** — not even partial or rotated. If credential rotation is the news, write "credential rotated" without quoting the value.
- **No private project IDs, internal dashboard URLs, or non-public hostnames** beyond what already appears in public READMEs.
- **No tester device serials, IP addresses, location data, or anything that could re-identify someone.**

If a sensitive detail is genuinely load-bearing for the decision, write *around* it: "the prod Supabase project" not its UUID, "the owner's primary inbox" not the address. The decision rationale almost never needs the literal value.

## Index

The directory listing is the index — `001` first, latest last. No separate index file to keep in sync.
