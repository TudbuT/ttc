cat > message.txt << EOF
> $(cat version.txt)

Additions:
none

Deletions:
none

Changes/Other:
none

Notes:
- Time taken (approx.): 1 hour

https://discord.gg/2WsVCQDpwy
EOF

nvim message.txt

git tag -aF message.txt $(cat version.txt)
git push --tags