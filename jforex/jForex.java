
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.*;


import com.dukascopy.api.IAccount;
import com.dukascopy.api.ITick;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.IUserInterface;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators.AppliedPrice;

import java.awt.Color;
import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;



public class jForex {

    private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
    private static String userName = "DEMODUKEHcST";
    private static String password = "EHcST";

    //static String[] columns = {"Open Time", "Id", "Label", "Comment", "Instrument", "Side", "Amount", "Original Amount", "Open Price", "Stop Loss", "Take Profit", "Profit (Pips)", "Profit Currency", "Profit in USD", "Commission", "Commission USD"};
    static String[] columns = {"Id", "Instrument", "Side", "Amount", "Open Price", "Stop Loss", "Take Profit"};
    //static String[][] data = {{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"},

    static String[][] data = new String[50][11];
    //static String[][] data = null;
    //static JFrame jf = new JFrame();

    public static void main(String[] args) throws Exception {       


        //get the instance of the IClient interface
        final IClient client = ClientFactory.getDefaultInstance();
        //set the listener that will receive system events
        client.setSystemListener(new ISystemListener() {
            private int lightReconnects = 12;

            public void onStart(long procid) {
                //IConsole console = context.getConsole();
                System.out.println("Strategy started: ");  
            }

            public void onStop(long processId) {
                System.out.println("Strategy stopped: " + processId);  
                if (client.getStartedStrategies().size() == 0) {
                    System.exit(0);
                }
            }

            @Override
            public void onConnect() {
                System.out.println("Connected");
                lightReconnects = 12;
            }

            @Override
            public void onDisconnect() {
                System.out.println("Disconnected");
                if (lightReconnects > 0) {
                    System.out.println("TRY TO RECONNECT, reconnects left: " + lightReconnects);
                    client.reconnect();
                    --lightReconnects;
                } else {
                    try {
                        //sleep for 10 seconds before attempting to reconnect
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                    try {
                        client.connect(jnlpUrl, userName, password);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });

        System.out.println("Connecting...");
        //connect to the server using jnlp, user name and password
        client.connect(jnlpUrl, userName, password);

        //wait for it to connect
        int i = 10; //wait max ten seconds
        while (i > 0 && !client.isConnected()) {
            Thread.sleep(1000);
            i--;
        }
        if (!client.isConnected()) {
            System.out.println("Failed to connect Dukascopy servers");
            System.exit(1);
        }
    Instrument[] instruments =  {
		
    //
    Instrument.EURUSD,
    Instrument.GBPUSD,
    Instrument.NZDUSD,
    Instrument.AUDUSD,
    //
    Instrument.EURGBP,
    //
    Instrument.USDCAD,
    Instrument.AUDCAD,
    Instrument.EURCAD,
    Instrument.GBPCAD,
    Instrument.NZDCAD,
    //
    Instrument.AUDCHF,
    Instrument.CADCHF,
    Instrument.EURCHF,
    Instrument.GBPCHF,
    Instrument.NZDCHF,
    Instrument.USDCHF,
    //
    Instrument.EURAUD,
    Instrument.GBPAUD,
    //
    Instrument.AUDNZD,
    Instrument.EURNZD,
    Instrument.GBPNZD,
    //
    Instrument.EURSGD,
    Instrument.USDSGD,
    Instrument.CHFSGD,
    Instrument.AUDSGD,
    //
    Instrument.AUDJPY,
    Instrument.USDJPY,
    Instrument.SGDJPY,
    Instrument.NZDJPY,
//    Instrument.HKDJPY,
    Instrument.CADJPY,
    Instrument.CHFJPY,
    Instrument.EURJPY,
    Instrument.GBPJPY,
    //
//  Instrument.USDHKD,
//    Instrument.EURHKD,
//    Instrument.CADHKD,
    //    //
//    Instrument.EURDKK,
//    Instrument.USDDKK,
    //
//    Instrument.USDPLN,
//    Instrument.EURPLN,
    //
//    Instrument.EURSEK,
//    Instrument.USDSEK,
    //
//    Instrument.USDNOK,
//    Instrument.EURNOK,
    //
//  Instrument.EURTRY,
//  Instrument.USDTRY,
    //
//    Instrument.USDMXN,
    //
//    Instrument.USDRUB,
    //
//  Instrument.USDZAR
    //
    };
        Set<Instrument> instrumentsSet = new HashSet<Instrument>();
        
		for( Instrument myInstrument : instruments  ) instrumentsSet.add(myInstrument);
		client.setSubscribedInstruments(instrumentsSet);
		int n = 10;
		while (client.getSubscribedInstruments().size() != instrumentsSet.size()) {
			try {
				System.out.println("Instruments not subscribed yet " + i);
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
			n--;
		}
		

        //start the strategy
        System.out.println("Starting strategy");
        final long strategyId = client.startStrategy(new priceDeviationAndOpoSide());
        //final long strategyId = client.startStrategy(new priceDeviation());
        //now it's running

        //every second check if "stop" had been typed in the console - if so - then stop the strategy
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {    

                Scanner s = new Scanner(System.in);             
                while(true){
                    while(s.hasNext()){
                        String str = s.next();
                        if(str.equalsIgnoreCase("stop")){
                            System.out.println("Strategy stop by console command.");
                            client.stopStrategy(strategyId);
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            });
        thread.start();

    }
}