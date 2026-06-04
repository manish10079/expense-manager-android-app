# Monetization Pricing Analysis Prompt

You are a Senior Product Strategist + Android Codebase Analyst.

Your job is to analyze this ExpenseTracker Android project and produce a monetization and packaging strategy based on the actual codebase, not assumptions.

## Project Context

- Android app built with Kotlin
- Clean Architecture + MVVM
- Monetization logic exists in the codebase
- I need a feature audit and a pricing/plan strategy
- Market focus: India
- Existing monetization may include free, ad-supported, premium, gated actions, temporary unlocks, privacy/security features, backup, sync, analytics, and customization

## Your Tasks

### 1. Analyze the codebase deeply

Find all app features that are:

- completely free
- ad-supported
- premium/pro-only
- partially gated by option
- hidden but implemented
- planned/incomplete but already scaffolded

Pay special attention to:

- `FeatureRegistry`
- monetization-related classes
- gated UI actions
- settings screens
- security/privacy features
- analytics features
- backup/export/import/sync related code
- transaction customization features
- search and advanced search features
- profile / personalization / automation / recurring / notification features

### 2. Produce a feature inventory

Create a table with:

- Feature name
- Code location
- Current access type
- User-facing value
- Whether it feels core or premium
- Whether it is suitable for:
  - Free
  - Ad-supported
  - Pro
  - Pro+

### 3. Identify the current monetization model in the code

- List all features in `FeatureRegistry`
- Identify which are:
  - `FREE`
  - `AD_SUPPORTED`
  - `PREMIUM`
  - option-level gated
- Explain any inconsistencies or missed monetization opportunities
- Point out features that are too weak to monetize
- Point out features that should not be ad-gated because of poor UX

### 4. Design two pricing plans

Design:

- `Pro`
- `Pro+`

Also define:

- `Free`

For each plan, list clearly:

- what is included
- what remains excluded
- what is ad-supported instead of subscription-locked
- why the grouping makes sense for users

### 5. Pricing strategy

Create a full India-market pricing recommendation for:

- monthly
- yearly
- optional launch pricing
- optional discounted annual anchor
- whether a free trial should be used
- whether ads should remain for some features even in Free tier

Your pricing output must include:

- recommended MRP
- positioning logic
- expected user psychology
- low-friction entry plan
- best-value annual plan
- Pro vs Pro+ differentiation

### 6. Optimize for business reality

Design the plans so they are:

- attractive to Indian users
- simple to understand
- profitable after Google Play commission
- sustainable for cloud features like:
  - multi-device sync
  - Google Drive backup
  - premium restore / migration
- aligned with actual feature value in this app

### 7. Special focus areas

Evaluate these separately:

- privacy/security features
- app lock / biometric / scrambled keypad
- analytics breakdowns
- auto backup / export / restore
- transaction card customization
- advanced search
- recurring features
- future cloud sync / multi-device sync
- Google Drive backup
- ad-reward temporary unlocks

### 8. Output format

Return the result in this structure:

#### A. Executive Summary

- short summary of current monetization state
- biggest opportunities
- biggest problems

#### B. Current Feature Audit

- feature table
- grouped by Free / Ad-Supported / Premium / Mixed

#### C. Code Evidence

- list important files and why they matter
- include file paths

#### D. Recommended Packaging

- Free plan
- Pro plan
- Pro+ plan

#### E. Recommended Pricing

- monthly/yearly price for Pro
- monthly/yearly price for Pro+
- launch pricing suggestion
- whether to use free trial

#### F. Monetization Improvements

- what should move from ad-supported to Pro
- what should move from Premium to Free
- what should be bundled into Pro+
- features that can justify cloud pricing

#### G. Risks and UX Warnings

- anything likely to hurt conversions or annoy users
- any pricing confusion risk
- any bad ad-gating decisions

#### H. Final Recommendation

- one final clean pricing model I should ship first

## Important Instructions

- Do not invent features that do not exist in code
- Prefer code evidence over assumptions
- If a feature is only partially implemented, mark it clearly
- If a feature is technically implemented but not surfaced in UI, mention that
- If a pricing decision depends on future cloud sync, clearly separate:
  - current codebase pricing
  - future cloud pricing
- Be opinionated and practical, not generic

## Optional Extra Outputs

If useful, also provide:

- a final polished pricing table suitable for Play Store listing
- a feature comparison matrix: Free vs Pro vs Pro+
