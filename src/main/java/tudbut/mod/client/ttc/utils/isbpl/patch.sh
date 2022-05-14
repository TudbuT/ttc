diff --horizon-lines=20 --unified ISBPL.java.original ISBPL.java > ISBPL.patch
cat ISBPL.patch
read
cp ~/gitshit/isbpl/ISBPL.java ISBPL.java
cp ~/gitshit/isbpl/ISBPL.java ISBPL.java.original
patch ISBPL.java ISBPL.patch && rm ISBPL.patch
