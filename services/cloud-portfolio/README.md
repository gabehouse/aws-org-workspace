# Gabriel House

Portfolio for three AWS systems: a multi-account organization, [House Audio](https://houseaudio.net), and Wilderchess.

[gabehouse.dev](https://gabehouse.dev/)

## What this repo is

A static Vite + React site. AWS Amplify builds it from GitHub and hosts it. Cloudflare serves DNS for the apex domain. Amplify is the host, not one of the systems on the page.

`amplify/` still defines a Cognito resource and a DynamoDB model from an older homepage. The site does not call either one. The model is kept so the next pipeline deploy does not drop its table.

## Local development

```bash
npm install
npm run dev
```
