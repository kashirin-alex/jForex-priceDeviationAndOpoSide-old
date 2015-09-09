1) place all the files in one folder on the server/desktop
2) set your ACCOUNT and PASSWORD in the file jforex.java at:
    private static String userName = "DUKASCOPY_account";
    private static String password = "DUKASCOPY_password";
3) on linux on the shell-command line (windows slightly different commands)
    a) compile: 
	javac -cp .:`find /home/jforex/libs/ -name "*.jar" | tr "\n" ":"` jForex.java
    b) run 
	nohup java -cp .:`find /home/jforex/libs/ -name "*.jar" | tr "\n" ":"` jForex &
    c) check it runs:
	ps aux | grep jforex

or execution on the jForex platform on the local-run (less steady)
or execution dukascopy remote run on the jforex platform, require to remove the GainBase file wrire functionality 
