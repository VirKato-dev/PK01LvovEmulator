0 CLS:COLOR 1,0
1 LINE (0,0)-(200,20),0,BF
2 LINE (0,20)-(200,40),1,BF
3 LINE (0,40)-(200,60),2,BF
4 LINE (0,60)-(200,80),3,BF
5 COLOR 1,C1,C2: LOCATE 1,9
6 PRINT "BACKGROUND"; C1; "PALETTE"; C2
7 INPUT "BACKGROUND, PALETTE"; C1,C2
8 IF C1=0 AND C2=0 GOTO 0
9 GOTO 5
