local:
scripts/run.sh
aws:
1. log in to role with credentials
2. export AWS_PROFILE={role}
3. change commented out line to point to eb url
4. ./scripts/deploy.sh {version}
