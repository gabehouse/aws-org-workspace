local:
scripts/run.sh
aws:
1. log in to role with credentials
2. export AWS_PROFILE={role}
3. change commented out line to point to eb url
4. ./scripts/deploy.sh {version}

run bots:
    1. push image to ecr: scripts/push_bot
    2. in main.tf change desired bots to 1 and apply, also change runbotvsbot to true and botIsAi to false (unless using ai to train)
