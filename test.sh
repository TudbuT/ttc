
cat > message.txt << EOF
> $(cat version.txt)

Additions:
none

Deletions:
none

Changes/Other:
none

Notes:
- Time taken (approx.): 1h

https://discord.gg/2WsVCQDpwy
EOF

git diff master > gitdiff
git log --oneline --graph --decorate > gitlog
vim -p message.txt gitdiff gitlog
rm gitdiff
