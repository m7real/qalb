# Qalb — Religious Content and Review Policy

## Purpose
This policy prevents Qalb from presenting fabricated, incomplete, or unreviewed religious information as established Quran or Sunnah.

## Content classes
1. **Curated and reviewed** — entered from an identifiable source and checked by a competent reviewer.
2. **Imported but not reviewed** — stored for personal use until checked.
3. **User-created** — personal dhikr, dua, notes, or text entered by the user.

The UI must visibly distinguish these classes. User-created content must never receive a verified/sunnah label automatically.

## Required metadata for curated records
- Exact Arabic wording.
- Translation and transliteration with their own review status.
- Source name and precise reference when available.
- Repetition count and occasion with a source/review note.
- Hadith grading only when a dependable reference supports it; preserve attribution and scholarly differences.
- Reviewer status and date.

## Do not do these things
- Do not invent references, hadith numbers, chains, grades, repetition counts, or timing windows.
- Do not paraphrase and display the result as the exact prophetic wording.
- Do not label a sunnah or recommended practice as obligatory without a correctly scoped, reviewed ruling.
- Do not automatically convert a general morning/evening dhikr into a post-prayer dhikr just because users often read it then.
- Do not let generative AI create or approve religious records at runtime in the MVP.
- Do not treat an external webpage as automatically correct without review.

## Timing and Hanafi fiqh
Timing of morning/evening adhkar and some related issues may have scholarly differences. Make the editorial rule explicit, record the source and review status, and avoid asserting that only one interpretation exists if a recognized difference is relevant. For a location-specific rule, consult a qualified reviewer familiar with Hanafi fiqh and local Bangladeshi practice.

## Suggested UI wording
- `সূত্রসহ যাচাইকৃত` only when the editorial process is actually completed.
- `যাচাই প্রয়োজন` for imported/unreviewed records.
- `ব্যক্তিগত যিকির` for user content.
- Avoid an ambiguous badge such as `সহিহ` if the evidence or scope is not sufficiently represented.

## Change process
A change to curated text, timing, repetition, translation, grading, or religious classification needs a review note and a test/update to seed data. Keep the review record in source control.
