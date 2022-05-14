cat version.txt
echo " <- old version"
read VERSION
echo -n "$VERSION" > version.txt
sed "s/$(cat previousVersion.txt)/$(cat version.txt)/g" < src/main/java/tudbut/mod/client/ttc/TTC.java > tmp.TTC.java
sed "s/$(cat previousVersion.txt)/$(cat version.txt)/g" < src/main/resources/mcmod.info > tmp.mcmod.info
mv tmp.mcmod.info src/main/resources/mcmod.info
mv tmp.TTC.java src/main/java/tudbut/mod/client/ttc/TTC.java
./gradlew jar
git commit -m "makerelease.sh: set version" version.txt build src/main/java/tudbut/mod/client/ttc/TTC.java src/main/resources/mcmod.info
git push
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
vim -p message.txt gitdiff
rm gitdiff

xdg-open "https://github.com/tudbut/ttc/releases/new" &

sleep 1
echo =========================================
cat message.txt
echo =========================================

cp version.txt previousVersion.txt
git commit -m "makerelease.sh: set previous version" previousVersion.txt
git push
git checkout master
git merge dev
read
git push
git tag -aF message.txt $(cat version.txt)
git push --tags
git checkout dev
