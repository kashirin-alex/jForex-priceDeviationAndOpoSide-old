
import com.dukascopy.api.*;
import com.dukascopy.api.Configurable;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.IUserInterface;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import java.math.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.text.SimpleDateFormat;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

public class priceDeviationAndOpoSide implements IStrategy {
    private IEngine engine;
    private IConsole console;
    private IHistory history;
    private IContext context;
    private IIndicators indicators;
    private IUserInterface userInterface;
    private IAccount account;
    
    private boolean strategyStatusCheck = true;
	
    private int slippage = 5;

    
    @Configurable("gain Base")
    public double gainBase = 88;
     
    @Configurable("gain EndOfDay")
    public double gainEndOfDayPercentage = 1.001;
    @Configurable("gain Percentage")
    public double gainPercentage= 1.007;
    @Configurable("gain Percentage to Close All")
    public double gainPercentageCloseAll= 1.03;
    
    @Configurable("min on Profit Close Pips")
    public double onProfitClosePips= 10;
    @Configurable("Profit Close multiplier of stdDev")
    public double ProfitCloseMultiStdDev = 0.1;
    
    @Configurable("Day End on Profit Close min Pips")
    public double onProfitClosePipsDayEnd= 10;
    @Configurable("Day End on Profit Close multiplier of stdDev")
    public double ProfitCloseDayEndMultiStdDev = 0.1;

    
    @Configurable("Amount")
    public double amount = 0.001;
    @Configurable("Value for 0.001")
    public double amountOne = 4;
     
    @Configurable("Max merges")
    public double maxMerges = 3;
    @Configurable("Merge Distance multiplier stdDev")
    public double mergeDist = 0.55;
    
    @Configurable("Number of Order")
    public int numOrders = 1;
      
    @Configurable("2nd order Distance from previus")
    public double distFromPrevOrder = 0.6;
    @Configurable("Distance as Profitable Multi 1st Step")
    public double distanceAsProfitable = 2;
    
    @Configurable("Stop loss")
    public double slPips = 100;
    @Configurable("Stop loss multiplier of stdDev")
    public double slStdDevMulti = 0.5;
    @Configurable("with Stop loss")
    public boolean withSetSL = false;

    @Configurable("Take profit")
    public double tpPips = 75;
    @Configurable("Take profit multiplier of stdDev")
    public double tpStdDevMulti = 0.5;
    @Configurable("with Take profit")
    public boolean withSetTP = true;
    
    @Configurable("with Trailling Step")
    public boolean withTraillingStep = true;
    @Configurable("Trailling 1st step")
    public double defFirstStep = 10;
    public double minFirstStep = 2.5;
    @Configurable("Trailling 1st step devider of MinMax")
    public double firstStepSTDdevider = 6;
	
    @Configurable("Min Trailling follow steps")
    public double minRestTralingStep = 20;
    @Configurable("Follow steps Multi of 1st")
    public double firstStepMulti = 2;
    
	
    @Configurable("STDdev timePeriod")
    public int STDminsPeriod = 168;
    @Configurable("STDdev Period")
    public Period STDPeriod = Period.ONE_HOUR;
    
    @Configurable(value = "Buy order",
            description = "Place a BUY order (long)")
    public boolean isBuyOrder = true;
    @Configurable(value = "Sell order",
            description = "Place a SELL order (short)")
    public boolean isSellOrder = true;

    
    @Configurable("manage Signals")
    public boolean manageSignals = false;
    @Configurable("by tick time")
    public boolean timeByTick = true;
    
    @Configurable("tick with different price")
    public boolean onlyDifferentTick = true;
    
    @Configurable("one Order Only")
    public boolean onlyOneOrder = false;
    @Configurable("one Order Value for 0.001")
    public double onlyOneOrderAmount = 20;
    
    @Configurable("email Reports")
    public boolean emailReports = true;
    @Configurable("Report Name")
    public String reportName = "Dukats";
    
    @Configurable("without Price Difference")
    public boolean withoutPriceDifference = false;
    
    @Configurable("without CCI")
    public boolean withoutCCI = false;
    
    @Configurable("without MA")
    public boolean withoutMA = false;
    
	
    public String dataFile = "/home/jforex/data/status.dat";
    public boolean dayEndClose = false;
    public long timeRate = 1;
    
    
    
    public ArrayList<Instrument> instruments = new ArrayList<>();
    
    public Instrument[] instrumentsFixedTesting =  {
    //Instrument.EURUSD
    Instrument.GBPJPY
    };
    
        
     
    public Instrument[] instrumentsFixed =  {
    //Instrument.GBPJPY,
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
    
    
	
    private long ordersExecutionLastRunTime=0;
    private long onAccountLastRunTime=0;
	
    private long onAccountEmailSendTime=0;
	private int onAccountEmailHourCount=0;
	private int onAccountEmailHour=25;
    private String onAccountLatestsOrders="";
    private String onAccountCloseOnProfit="";
    private String newLine="<br>";
	
    private long executeInstrumentsLastRunTime=0;
    private boolean ordersExecutionLastRunCheck = false;
    private boolean onStartSetBaseGain = false;

    private Map<String, Long> instrumentsPendingOrderLastRunTime= new HashMap<String, Long>();

    
    private Map<String, Boolean> instrumentsBusy = new HashMap<String, Boolean>();
    
    private Map<String, Boolean> instrumentSTDdevBusy = new HashMap<String, Boolean>();
    private Map<String, Double> instrumentsSTDdev= new HashMap<String, Double>();
    private Map<String, Double> instrumentsSTDdevAVG= new HashMap<String, Double>();
    private Map<String, Long> instrumentsSTDdevTime= new HashMap<String, Long>();
    
    private Map<String, Long> ordersBusyTP= new HashMap<String, Long>();
    private Map<String, Long> ordersBusySL= new HashMap<String, Long>();

    private Map<String, Boolean> instrumentDirectionBusy = new HashMap<String, Boolean>();
    
    private Map<String, Double> instrumentsTimeFrame= new HashMap<String, Double>();
    private Map<String, Map<String, Double>> instrumentsTimeFrames= new HashMap<String, Map<String, Double>>();
    private Map<String, Long> instrumentsTimeFrameTime = new HashMap<String, Long>();
    private Map<String, Boolean> instrumentsTimeFrameBusy = new HashMap<String, Boolean>();

    
    
    private Map<String, Double> instrumentsLastPrice = new HashMap<String, Double>();
    private Map<String, String> instrumentsTrendInfo = new HashMap<String, String>();

	private ICurrency accountCurrency;
	
    private double minStdDev;
    private long timeMillisSet = 0;
	private long checkTimer=0;
    
    public long getTimeMillis() {
        if(!timeByTick) return System.currentTimeMillis();
        return timeMillisSet;
    }
    public void setTimeMillis(long time) {
        if(time > timeMillisSet) {
            timeMillisSet = time;
            
            //console.getOut().println("timeMillisSet: " +timeMillisSet);   
        }
    }
	
	public void setGainBase() {
		onStartSetBaseGain = true;
		try {
			File file=new File(dataFile);       
			if(!file.exists()){
				file.createNewFile();
			} else {
				try{
					FileReader fr = new FileReader(file);
					char [] a = new char[1];
					fr.read(a);
					System.out.print(a); //prints the characters one by one
					fr.close();
					Double savedEQ = Double.parseDouble(String.valueOf(a[0]));
					if(Double.compare(savedEQ,gainBase) > 0 && !Double.isNaN(savedEQ))
						gainBase = savedEQ;
				} catch (Exception e) {
					console.getErr().println("setGainBase E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
				}
			}
			FileWriter writer = new FileWriter(file); 
			writer.write(Double.toString(round(gainBase,0))); 
			writer.flush();
			writer.close();
			
        } catch (Exception e) {
			console.getErr().println("setGainBase E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
		}
	}
	
    @Override
    public void onStart(IContext context) {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.context = context;
        this.indicators = context.getIndicators();
        this.userInterface = context.getUserInterface();
        
        //console.getErr().println("onStart start");
        minStdDev = minFirstStep*firstStepSTDdevider+0.5;
        
        IAccount account = context.getAccount();
		accountCurrency = account.getAccountCurrency();
		
		maxMerges++;
        
		try {
			setGainBase();
			
            double lastPrice, rt;
            for( Instrument myInstrument : instrumentsFixed ) {
				if(onlyDifferentTick) {
					if (!instrumentsLastPrice.containsKey(myInstrument.toString()+"_Ask")) {
						instrumentsLastPrice.put(myInstrument.toString()+"_Ask", history.getLastTick(myInstrument).getAsk());
						instrumentsLastPrice.put(myInstrument.toString()+"_Bid", history.getLastTick(myInstrument).getBid());
					}
				}
				instrumentsTrendInfo.put(myInstrument.toString()+"_DW","");
				instrumentsTrendInfo.put(myInstrument.toString()+"_UP","");
					
            } 
    
        
                
            Date now = new Date(); 
            console.getOut().println( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(now));

			
			
			
        
            if(timeByTick) setTimeMillis(0);
            setInstuments();
            ordersExecutionLastRunTime =  getTimeMillis()-6000/timeRate;
        
            for( Instrument myInstrument : instruments  ) { // context.getSubscribedInstruments() 
            context.setSubscribedInstruments(java.util.Collections.singleton(myInstrument), true);
            
                //if(timeByTick) { 
                //    setInstrument(myInstrument);
                //    setTimeMillis(history.getLastTick(myInstrument).getTime());
                //}
            
                instrumentsPendingOrderLastRunTime.put(myInstrument.toString(), getTimeMillis()+15000/timeRate);
            } 
        
        } catch (Exception e) {
            //console.getErr().println("onStart E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }
        //console.getErr().println("onStart end");
    }


        
    
    @Override
    public void onTick(Instrument instrument, ITick tick) {
       try {  
			if(!onStartSetBaseGain) setGainBase();
			if(strategyStatusCheck) {
				if(checkTimer + 10 * 1000 < System.currentTimeMillis()) {
					checkTimer = System.currentTimeMillis();
					console.getOut().println( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(new Date()));
				}
			}

	   
	   
			if(onlyDifferentTick) {
				if(Double.compare(instrumentsLastPrice.get(instrument.toString()+"_Ask"), tick.getAsk()) == 0 && Double.compare(instrumentsLastPrice.get(instrument.toString()+"_Bid"), tick.getBid()) == 0) 
					return;
				instrumentsLastPrice.put(instrument.toString()+"_Ask", tick.getAsk());
				instrumentsLastPrice.put(instrument.toString()+"_Bid", tick.getBid());
			}

            if(timeByTick) setTimeMillis(tick.getTime());
        
            boolean subscribedInstrument = false;
            for(Instrument myInstrument : instruments) if(myInstrument == instrument) subscribedInstrument = true;
            if (!subscribedInstrument  && !manageSignals) return;
			

            final trailingTakeProfit trailingTakeProfitTASK = new trailingTakeProfit(instrument);
            //run the task in a different thread:
            final IContext finalContext = context;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        finalContext.executeTask(trailingTakeProfitTASK);
                    } catch (Exception e) {
                   //console.getErr().println("onTick E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
                    }
                }
            });
            thread.start();
        
            if (!instrumentsBusy.containsKey(instrument.toString())) {
                instrumentsBusy.put(instrument.toString(), false);
                setInstrument(instrument);
            } else if(instrumentsBusy.get(instrument.toString())== false)
                setInstrument(instrument);
                   
            if(ordersExecutionLastRunTime < getTimeMillis() - 60 * 1000/timeRate) {
                ordersExecutionLastRunTime =  getTimeMillis();
                for( Instrument myInstrument : instruments  ) {
                    if (!instrumentsPendingOrderLastRunTime.containsKey(myInstrument.toString()))
                        instrumentsPendingOrderLastRunTime.put(myInstrument.toString(),getTimeMillis());
                    context.setSubscribedInstruments(java.util.Collections.singleton(myInstrument), true);
                    if (!instrumentsBusy.containsKey(myInstrument.toString())) {
                        instrumentsBusy.put(instrument.toString(), false);
                        setInstrument(myInstrument);
                    } else if(instrumentsBusy.get(myInstrument.toString())== false)
                        setInstrument(myInstrument);
                } 
            }
        } catch (Exception e) {
           //console.getErr().println("onTick E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        } 
    }


    public void setInstrument(Instrument instrument){
        try {         
        
            if(instrumentsPendingOrderLastRunTime.get(instrument.toString()) > getTimeMillis() - 10000/timeRate) return;
			
			if(onlyOneOrder){
				if(executeInstrumentsLastRunTime+3000 >= getTimeMillis()) return;
				executeInstrumentsLastRunTime = getTimeMillis();
			}
            final executeInstrument executeInstrumentTASK = new executeInstrument(instrument);
        
            //run the task in a different thread:
            final IContext finalContext = context;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        finalContext.executeTask(executeInstrumentTASK);
                    } catch (Exception e) {
                        //console.getErr().println("finalContext.executeTask E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            //console.getErr().println("setInstrument E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }
    }
        
    private class executeInstrument implements Callable<Instrument> {
        private final Instrument instrument;

        public executeInstrument(Instrument instrument) {
            this.instrument = instrument;
            
        }

        public Instrument call() throws Exception {
        
            int totalBuyOrder = 0;
            int totalSellOrder = 0;
			
            boolean newBuyOrder = true;
            boolean newSellOrder = true;
            boolean positiveBuyOrder = false;
            boolean positiveSellOrder = false;
            try {                  
                if(instrumentsBusy.get(instrument.toString()) == true) return instrument;
                //if(account.getUseOfLeverage() >=100) return instrument;
                
				if(onlyOneOrder) {
					if(engine.getOrders().size() > 0) {
						return instrument;
					}
				}
                instrumentsBusy.put(instrument.toString(), true);
      

     
                for( IOrder order : engine.getOrders(instrument)) {
                    if(!order.getLabel().contains("Signal:")) {
                        if(order.getState() == IOrder.State.FILLED || order.getState() == IOrder.State.OPENED ||  order.getState() == IOrder.State.CREATED){
                            if (order.getOrderCommand() == OrderCommand.BUY || order.getOrderCommand() == OrderCommand.PLACE_BID || order.getOrderCommand() == OrderCommand.BUYLIMIT || order.getOrderCommand() == OrderCommand.BUYSTOP) {
                                totalBuyOrder ++;
                                if(order.getOrderCommand() == OrderCommand.PLACE_BID) { totalBuyOrder ++; newBuyOrder = false;}
                                if (order.getOrderCommand() == OrderCommand.BUY) {
                                    if (Double.compare(order.getStopLossPrice(), 0) == 0 || Double.compare(order.getTakeProfitPrice(), 0) == 0)
                                        setTPandSL(order,instrument);
                                    
                                    double BuyOrderProfitLossInPips = order.getProfitLossInPips();
                                    double stdDevAVG = getStdDev(instrument);
									
                                    if(Double.compare((distFromPrevOrder*stdDevAVG)/instrument.getPipValue(), Math.abs(BuyOrderProfitLossInPips)) > 0 ) newBuyOrder = false;
                                    
                                    if(Double.compare(order.getTrailingStep(), 0) > 0) {
                                        totalBuyOrder --; newBuyOrder = true;
										positiveBuyOrder = true;
                                    } 
									
									//if(Double.compare(BuyOrderProfitLossInPips*instrument.getPipValue(),-getFirstStep(instrument)*distanceAsProfitable) >= 0) {
									//	positiveBuyOrder = true;
									//}
                                    //console.getErr().println("pipDist: "+pipDist/instrument.getPipValue()*10 +" BuyOrderProfitLossInPips: "+BuyOrderProfitLossInPips+" " +instrument.toString());
                                }
                                if(order.getFillTime() > getTimeMillis()-10*1000) { newBuyOrder = false;}
                            }else if(order.getOrderCommand() == OrderCommand.SELL || order.getOrderCommand() == OrderCommand.PLACE_OFFER || order.getOrderCommand() == OrderCommand.SELLLIMIT || order.getOrderCommand() == OrderCommand.SELLSTOP) {
                                totalSellOrder ++;
                                if(order.getOrderCommand() == OrderCommand.PLACE_OFFER) {  totalSellOrder ++; newSellOrder = false;}
                                if (order.getOrderCommand() == OrderCommand.SELL) {
                                    if (Double.compare(order.getStopLossPrice(), 0) == 0 || Double.compare(order.getTakeProfitPrice(), 0) == 0)
                                        setTPandSL(order,instrument);
                        
                                    double SellOrderProfitLossInPips = order.getProfitLossInPips();
                                    double stdDevAVG = getStdDev(instrument);
									
                                    if(Double.compare((distFromPrevOrder*stdDevAVG)/instrument.getPipValue(), Math.abs(SellOrderProfitLossInPips)) > 0) newSellOrder = false;
									
                                    
                                    if(Double.compare(order.getTrailingStep(), 0) > 0) { 
                                        totalSellOrder --; newSellOrder = true; 
										positiveSellOrder = true;
                                    } 
									
									//if(Double.compare(SellOrderProfitLossInPips*instrument.getPipValue(),-getFirstStep(instrument)*distanceAsProfitable) >= 0) {
									//	positiveSellOrder = true;
									//}
                                        //console.getErr().println("pipDist: "+pipDist/instrument.getPipValue()*10 +" SellOrderProfitLossInPips: "+SellOrderProfitLossInPips+" " +instrument.toString());
                                }
                                if(order.getFillTime() > getTimeMillis()-10*1000) { newSellOrder = false;}
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //console.getErr().println("Instrument getOrders call() E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
            }  
            
			
            try {   
                if (totalBuyOrder < numOrders+1 && isBuyOrder && newBuyOrder && !positiveSellOrder) {
					if(getTrend(instrument, OfferSide.ASK).equals("UP"))
						submitOrder(instrument, getAmount(instrument), OrderCommand.BUY);
				}
                if (totalSellOrder < numOrders+1 && isSellOrder && newSellOrder && !positiveBuyOrder) {
					if(getTrend(instrument, OfferSide.BID).equals("DW"))
						submitOrder(instrument, getAmount(instrument), OrderCommand.SELL);
				}
                
            } catch (Exception e) {
                //console.getErr().println("Instrument call() submitOrder E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
            } 
        
            instrumentsBusy.put(instrument.toString(), false);
            return instrument;
        }
    }   
        
    
    private void submitOrder(Instrument instrument, double amountCurrrent, OrderCommand orderCmd) {

        try{
            if(instrumentsPendingOrderLastRunTime.get(instrument.toString()) > getTimeMillis() - 3000) return;
            instrumentsPendingOrderLastRunTime.put(instrument.toString(), getTimeMillis());
            
            double slPrice, tpPrice, at, tmpPips;
        
            double instumrntPipValue = instrument.getPipValue();
            int instrumentPipScale = instrument.getPipScale();
        
            if (orderCmd == OrderCommand.BUY) {
                double lastPrice = history.getLastTick(instrument).getAsk();
                
                tpPrice = 0;
                if(withSetTP) {        
                    tmpPips = getStdDev(instrument)*tpStdDevMulti;
                    if(Double.compare(tmpPips, tpPips*instumrntPipValue) <=  0)  tmpPips = tpPips*instumrntPipValue;
                    tpPrice = lastPrice + tmpPips;
                    tpPrice = round(tpPrice, instrumentPipScale + 1);
                }
            
                slPrice = 0;
                if(withSetSL) {    
                    tmpPips = getStdDev(instrument)*slStdDevMulti;        
                    if(Double.compare(tmpPips, slPips*instumrntPipValue) <=  0)  tmpPips = slPips*instumrntPipValue;
                    slPrice = lastPrice - tmpPips;
                    slPrice = round(slPrice, instrumentPipScale + 1);
                }
            
                at = lastPrice;
                //at = round(at, instrumentPipScale + 1);       
            
                if(Double.compare(at,lastPrice) <= 0){
						
                    if(Double.compare(tpPrice, 0) == 0 && Double.compare(slPrice, 0) == 0) {
                        IOrder newOrder = engine.submitOrder("O"+getTimeMillis()+"_UP" , instrument, orderCmd, amountCurrrent);
                        newOrder.waitForUpdate(888);  
                    } else {
                        IOrder newOrder = engine.submitOrder("O"+getTimeMillis()+"_UP", instrument, orderCmd, amountCurrrent, at, slippage, slPrice, tpPrice);
                        ordersBusySL.put(newOrder.getId(),getTimeMillis()+2500/timeRate); 
                        ordersBusyTP.put(newOrder.getId(),getTimeMillis()+2500/timeRate); 
                        newOrder.waitForUpdate(888);  
                    }
					onAccountLatestsOrders += ""+newLine+""+newLine+""+instrument.toString()+"-"+orderCmd.toString()+" amt:"+amountCurrrent+ "at:"+at+" sl:"+slPrice+" tp:"+tpPrice+""+newLine+" info:"+newLine+""+instrumentsTrendInfo.get(instrument.toString()+"_UP");
					instrumentsTrendInfo.put(instrument.toString()+"_UP","");
                    //console.getInfo().format("%s at:%s sl:%s tp:%s LT:%s A:%s inst:%s", orderCmd.toString(), at, slPrice, tpPrice, history.getLastTick(instrument).getAsk() ,amount, instrument.toString()).println();
                }
            } else if (orderCmd == OrderCommand.SELL) { 
                double lastPrice = history.getLastTick(instrument).getBid();
                
                tpPrice = 0;
                if(withSetTP) {        
                    tmpPips = getStdDev(instrument)*tpStdDevMulti;
                    if(Double.compare(tmpPips, tpPips*instumrntPipValue) <=  0)  tmpPips = tpPips*instumrntPipValue;
                    tpPrice = lastPrice - tmpPips;
                    tpPrice = round(tpPrice, instrumentPipScale + 1);
                }
            
                slPrice = 0;
                if(withSetSL) {    
                    tmpPips = getStdDev(instrument)*slStdDevMulti;        
                    if(Double.compare(tmpPips, slPips*instumrntPipValue) <=  0)  tmpPips = slPips*instumrntPipValue;
                    slPrice = lastPrice + tmpPips;
                    slPrice = round(slPrice, instrumentPipScale + 1);
                }

                at = lastPrice;
                //at = round(at, instrumentPipScale + 1);
            
                if(Double.compare(at,lastPrice) >= 0){
                    
                    if(Double.compare(tpPrice, 0) == 0 && Double.compare(slPrice, 0) == 0) {
                        IOrder newOrder = engine.submitOrder("O"+getTimeMillis()+"_DW", instrument, orderCmd, amountCurrrent);
                        newOrder.waitForUpdate(888);  
                    } else {
                        IOrder newOrder = engine.submitOrder("O"+getTimeMillis()+"_DW", instrument, orderCmd, amountCurrrent, at, slippage, slPrice, tpPrice);
                        ordersBusySL.put(newOrder.getId(),getTimeMillis()+2500/timeRate); 
                        ordersBusyTP.put(newOrder.getId(),getTimeMillis()+2500/timeRate); 
                        newOrder.waitForUpdate(888);  
                    }
					
					onAccountLatestsOrders += ""+newLine+""+newLine+""+instrument.toString()+"-"+orderCmd.toString()+" amt:"+amountCurrrent+ "at:"+at+" sl:"+slPrice+" tp:"+tpPrice+""+newLine+" info:"+newLine+""+instrumentsTrendInfo.get(instrument.toString()+"_DW");
					instrumentsTrendInfo.put(instrument.toString()+"_DW","");
                    //console.getInfo().format("%s at:%s sl:%s tp:%s LT:%s PD:%s A:%s inst:%s", orderCmd.toString(), at, slPrice, tpPrice, history.getLastTick(instrument).getAsk(), pipsDistance ,amount, instrument.toString()).println();
                }
            }
        } catch (Exception e) {            
           //console.getErr().println("submitOrder E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e + " " +instrument.toString());
        }
    }
        
    
    
    
    private class trailingTakeProfit implements Callable<Instrument> {
        private final Instrument instrument;

        public trailingTakeProfit(Instrument instrument) {
            this.instrument = instrument;
        }

        public Instrument call() throws Exception {
            try {

                if(!withTraillingStep)
                return instrument;            
            
                double instumrntPipValue = instrument.getPipValue();
                int instrumentPipScale = instrument.getPipScale();
                List<IOrder> buyOrders = new ArrayList<IOrder>();
                List<IOrder> sellOrders = new ArrayList<IOrder>();
                 
                boolean manageSignalsOrder = false;
                for( IOrder order : engine.getOrders(instrument)) {
                    if(order.getState() == IOrder.State.FILLED || order.getState() == IOrder.State.OPENED){
						OrderCommand cmd = order.getOrderCommand();
						if ((cmd == OrderCommand.BUY || cmd == OrderCommand.SELL) && (!order.getLabel().contains("Signal:") || (order.getLabel().contains("Signal:") && manageSignals))) {
							
                            if(!order.getLabel().contains("Signal:")) {
                                if(Double.compare(Math.abs(order.getProfitLossInPips()),(getStdDev(instrument)/instumrntPipValue)*mergeDist) > 0){
                                    if (cmd == OrderCommand.SELL) {
                                        if( order.getAmount()/getAmount(instrument) < maxMerges-1) {
                                            sellOrders.add(order);
                                        }
                                    }
                                    if (cmd == OrderCommand.BUY) {
                                        if( order.getAmount()/getAmount(instrument) < maxMerges-1) {
                                            buyOrders.add(order);
                                        }
                                    }
                                }
                            }
                        
                        
                            if (!ordersBusySL.containsKey(order.getId()))
								ordersBusySL.put(order.getId(),getTimeMillis()-5100/timeRate);  
                            
                            if(ordersBusySL.get(order.getId()) < getTimeMillis()-888/timeRate) { //  && Double.compare(order.getTrailingStep(), 0) == 0
							
								double trailingFirstStep= 0;
								double trailingRestSteps = 0;
								double firstStep = getFirstStep(instrument);
								double pipDistance = setPipsDistance(instrument,((cmd == OrderCommand.BUY)?OfferSide.ASK:OfferSide.BID)); 
							    
                                if(Double.compare(firstStep,defFirstStep*instumrntPipValue) < 0)
                                    firstStep = defFirstStep*instumrntPipValue;
								
								trailingFirstStep = pipDistance/firstStepSTDdevider;
                                if(Double.compare(trailingFirstStep,firstStep) < 0)
                                    trailingFirstStep = firstStep;
                                if(order.getLabel().contains("Signal:") && manageSignals) 
                                    trailingFirstStep = trailingFirstStep *2;
                                    
                                trailingRestSteps = trailingFirstStep*firstStepMulti;  
                                if(Double.compare(trailingRestSteps,minRestTralingStep*instumrntPipValue) < 0)
                                    trailingRestSteps = minRestTralingStep*instumrntPipValue;
                                	
                                double trailingStep = (trailingRestSteps/instumrntPipValue)*2;
								
                                if(Double.compare(order.getProfitLossInPips()*instumrntPipValue,trailingFirstStep*2) >= 0){
									
									if (cmd == OrderCommand.BUY) {
                                        double lastTickBid = history.getLastTick(instrument).getBid();
                                        double lastTickAsk = history.getLastTick(instrument).getAsk();
                                        double trailSLprice = lastTickBid - trailingFirstStep - (lastTickAsk-lastTickBid);
                                        
                                        if(Double.compare(order.getProfitLossInPips()*instumrntPipValue,trailingRestSteps) >= 0 && Double.compare(order.getTrailingStep(), 0) != 0)
                                            trailSLprice = lastTickBid - trailingRestSteps;
                                            
                                        trailSLprice = round(trailSLprice, instrumentPipScale + 1); 
                                 
                                        if(Double.compare(trailSLprice,0) > 0 && Double.compare(order.getOpenPrice(),trailSLprice) < 0 ){
                                            if(Double.compare(order.getStopLossPrice(),trailSLprice) < 0 || Double.compare(order.getStopLossPrice(),0) == 0) {
                                                if(ordersBusySL.get(order.getId()) < getTimeMillis()-1890/timeRate) {
                                                    if(Double.compare(order.getTrailingStep(), 0) == 0 || Double.compare(order.getStopLossPrice(),trailSLprice - trailingRestSteps) < 0 ) {
                                                        ordersBusySL.put(order.getId(),getTimeMillis()+10000/timeRate);
                                                        order.setStopLossPrice(trailSLprice, OfferSide.BID, trailingStep);
                                                        if(Double.compare(order.getTakeProfitPrice(), 0) != 0) {
															order.setTakeProfitPrice(0);
															ordersBusyTP.put(order.getId(),getTimeMillis()+10000/timeRate);
														}
                                                        order.waitForUpdate(1890);
                                                    }
                                                }
                                            }
                                        }
                                    }else if(cmd == OrderCommand.SELL) {
                                        double lastTickAsk = history.getLastTick(instrument).getAsk();
                                        double lastTickBid = history.getLastTick(instrument).getBid();
                                        double trailSLprice = lastTickAsk + trailingFirstStep + (lastTickAsk-lastTickBid);
                                        
                                        if(Double.compare(order.getProfitLossInPips()*instumrntPipValue,trailingRestSteps) >= 0 && Double.compare(order.getTrailingStep(), 0) != 0)
                                            trailSLprice = lastTickAsk + trailingRestSteps;
                                            
                                        trailSLprice = round(trailSLprice, instrumentPipScale + 1); 

                                        if(Double.compare(trailSLprice,0) > 0 && Double.compare(order.getOpenPrice(),trailSLprice) > 0 ){
                                            
                                            if(Double.compare(order.getStopLossPrice(),trailSLprice) > 0 || Double.compare(order.getStopLossPrice(),0) == 0) {
                                                if(ordersBusySL.get(order.getId()) < getTimeMillis()-1890/timeRate) {
                                                    if(Double.compare(order.getTrailingStep(), 0) == 0 || Double.compare(order.getStopLossPrice(),trailSLprice + trailingRestSteps) > 0 ) {
                                                        ordersBusySL.put(order.getId(),getTimeMillis()+10000/timeRate);
                                                        order.setStopLossPrice(trailSLprice , OfferSide.ASK, trailingStep); //
                                                        if(Double.compare(order.getTakeProfitPrice(), 0) != 0) {
															order.setTakeProfitPrice(0);
															ordersBusyTP.put(order.getId(),getTimeMillis()+10000/timeRate);
														}
                                                        order.waitForUpdate(1890);
                                                    }
                                                }
                                            }
                                        }
									}
								}
								
                            }
							
                        }
                    }
                }
                
                
                
                
                
                ////
                boolean mergeSL = true;
                boolean mergeTP = true;
                if(buyOrders.size() >= 2) {
                    for(IOrder o: buyOrders){
                        if (ordersBusySL.containsKey(o.getId())) {
                            if(ordersBusySL.get(o.getId()) < getTimeMillis()) {
                                if(Double.compare(o.getStopLossPrice(),0) > 0){
                                    o.setStopLossPrice(0);
                                    ordersBusySL.put(o.getId(),getTimeMillis()+2500/timeRate);        
                                    o.waitForUpdate(2000);  
                                    mergeSL = false;                  
                                }
                            }
                        }
                        if (ordersBusyTP.containsKey(o.getId())) {
                            if(ordersBusyTP.get(o.getId()) < getTimeMillis()) {
                                if(Double.compare(o.getTakeProfitPrice(),0) > 0){
                                    o.setTakeProfitPrice(0);
                                    ordersBusyTP.put(o.getId(),getTimeMillis()+2500/timeRate);    
                                    o.waitForUpdate(2000);    
                                    mergeTP = false;                    
                                }  
                            }
                        }
                    } 
                    if(mergeTP && mergeSL) {
                        IOrder mergedOrder = engine.mergeOrders(buyOrders.get(0).getOrderCommand().toString() + getTimeMillis(), buyOrders.get(0), buyOrders.get(1));
                        ordersBusyTP.put(mergedOrder.getId(),getTimeMillis()+3000/timeRate);
                        ordersBusySL.put(mergedOrder.getId(),getTimeMillis()+3000/timeRate);
                        mergedOrder.waitForUpdate(2000);
                        setTPandSL(mergedOrder,instrument);
                    }
                }
                
                mergeSL = true;
                mergeTP = true;
                if(sellOrders.size() >= 2) {
                    for(IOrder o: sellOrders){
                        if (ordersBusySL.containsKey(o.getId())) {
                            if(ordersBusySL.get(o.getId()) < getTimeMillis()) {
                                if(Double.compare(o.getStopLossPrice(),0) > 0){
                                    o.setStopLossPrice(0);
                                    ordersBusySL.put(o.getId(),getTimeMillis()+2500/timeRate);       
                                    o.waitForUpdate(2000);  
                                    mergeSL = false;                    
                                }
                            }    
                        }
                        if (ordersBusyTP.containsKey(o.getId())) {
                            if(ordersBusyTP.get(o.getId()) < getTimeMillis()) {
                                if(Double.compare(o.getTakeProfitPrice(),0) > 0){
                                    o.setTakeProfitPrice(0);
                                    ordersBusyTP.put(o.getId(),getTimeMillis()+2500/timeRate);     
                                    o.waitForUpdate(2000);    
                                    mergeTP = false;                                
                                }
                            }
                        }  
                    }
                    if(mergeTP && mergeSL) {
                        IOrder mergedOrder = engine.mergeOrders(sellOrders.get(0).getOrderCommand().toString() + getTimeMillis() , sellOrders.get(0), sellOrders.get(1));
                        ordersBusyTP.put(mergedOrder.getId(),getTimeMillis()+3000/timeRate);
                        ordersBusySL.put(mergedOrder.getId(),getTimeMillis()+3000/timeRate);
                        mergedOrder.waitForUpdate(2000);
                        ordersBusyTP.put(mergedOrder.getId(),getTimeMillis()-240000/timeRate);
                        ordersBusySL.put(mergedOrder.getId(),getTimeMillis()-240000/timeRate);
                       
                        setTPandSL(mergedOrder,instrument);
                    }
                }
                
            } catch (Exception e) {
                //console.getErr().println("trailingTakeProfit E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e + " " + instrument.toString());
            }
            return instrument;
        }
    }

        
    private void setTPandSL(IOrder order, Instrument instrument) {

            try {    
                if(!withSetTP && !withSetSL) return;
                
                if(Double.compare(order.getTrailingStep(), 0) > 0)
                    return;
                if(Double.compare(order.getStopLossPrice(),0) > 0 && !withSetTP && withSetSL) 
                    return;
                if(Double.compare(order.getTakeProfitPrice(),0) > 0 && !withSetSL && withSetTP) 
                    return;
                    
                if (!ordersBusyTP.containsKey(order.getId()))
                    ordersBusyTP.put(order.getId(),getTimeMillis()-31111/timeRate);  
                
                if (!ordersBusySL.containsKey(order.getId()))
                    ordersBusySL.put(order.getId(),getTimeMillis()-31111/timeRate);  
  
                double slPrice, tpPrice, openPrice;
                double instumrntPipValue = instrument.getPipValue();
                int instrumentPipScale = instrument.getPipScale();

                if (order.getOrderCommand() == OrderCommand.BUY) {
                    if(withSetTP) {
                        if(ordersBusyTP.get(order.getId()) < getTimeMillis()-240000/timeRate) {
                            openPrice = order.getOpenPrice();
                        
                            double tpPipsTMP = getStdDev(instrument)*tpStdDevMulti;//getMinMaxDif(instrument);
                            if(Double.compare(tpPipsTMP, tpPips*instumrntPipValue) <= 0)  tpPipsTMP = tpPips*instumrntPipValue;

                            tpPrice = openPrice + tpPipsTMP;
                            tpPrice = round(tpPrice, instrumentPipScale + 1);
                            order.setTakeProfitPrice(tpPrice);
                            ordersBusyTP.put(order.getId(),getTimeMillis()+1000/timeRate);
                            order.waitForUpdate(888);
                        }
                    }
                    if(withSetSL) {
                        if(ordersBusySL.get(order.getId()) < getTimeMillis()-240000/timeRate) {
                            openPrice = order.getOpenPrice();
                        
                            double slPipsTMP = getStdDev(instrument)*slStdDevMulti;//getMinMaxDif(instrument);
                            if(Double.compare(slPipsTMP, slPips*instumrntPipValue) <= 0) slPipsTMP = slPips/instumrntPipValue;
                        
                            slPrice = openPrice - slPipsTMP;
                            slPrice = round(slPrice, instrumentPipScale + 1);
                            order.setStopLossPrice(slPrice);
                            ordersBusySL.put(order.getId(),getTimeMillis()+1000/timeRate);  
                            order.waitForUpdate(888);
                        }
                    }        
                    //console.getInfo().format("%s at:%s sl:%s tp:%s LT:%s", order.getOrderCommand().toString(), lastPrice, slPrice, tpPrice, history.getLastTick(instrument).getBid()).println();
                    
                } else if (order.getOrderCommand() == OrderCommand.SELL) {
                    
                    if(withSetTP) {
                        if(ordersBusyTP.get(order.getId()) < getTimeMillis()-240000/timeRate) {
                            openPrice = order.getOpenPrice();
                        
                            double tpPipsTMP =  getStdDev(instrument)*tpStdDevMulti;//getMinMaxDif(instrument);
                            if(Double.compare(tpPipsTMP, tpPips*instumrntPipValue) <= 0)  tpPipsTMP = tpPips*instumrntPipValue;

                            tpPrice = openPrice - tpPipsTMP;
                            tpPrice = round(tpPrice, instrumentPipScale + 1);
                            order.setTakeProfitPrice(tpPrice);
                            ordersBusyTP.put(order.getId(),getTimeMillis()+1000/timeRate);  
                            order.waitForUpdate(888);
                        }
                    }    
                    if(withSetSL) {
                        if(ordersBusySL.get(order.getId()) < getTimeMillis()-240000/timeRate) {
                            openPrice = order.getOpenPrice();
                        
                            double slPipsTMP =  getStdDev(instrument)*slStdDevMulti;//getMinMaxDif(instrument);
                            if(Double.compare(slPipsTMP, slPips*instumrntPipValue) <= 0) slPipsTMP = slPips*instumrntPipValue;
                        
                            slPrice = openPrice + slPipsTMP;
                            slPrice = round(slPrice, instrumentPipScale + 1);
                            order.setStopLossPrice(slPrice);
                            ordersBusySL.put(order.getId(),getTimeMillis()+1000/timeRate);  
                            order.waitForUpdate(888);
							
                        }
                    }
                    //console.getInfo().format("%s at:%s sl:%s tp:%s LT:%s", order.getOrderCommand().toString(), lastPrice, slPrice, tpPrice, history.getLastTick(instrument).getBid()).println();
                }
            } catch (Exception e) {
               //console.getErr().println("setTPandSL E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
            }
        
    }
    
    
    
    
    @Override
    public void onAccount(IAccount account) {
        try {
            
            if(onAccountLastRunTime > getTimeMillis() - 10 * 1000/timeRate || ordersExecutionLastRunCheck) 
                return;
			
            ordersExecutionLastRunCheck = true;
            onAccountLastRunTime =  getTimeMillis();
            closeOrdesOnProfit(account);
            
            setInstuments();
			
			if(onlyOneOrder) {
                amount = round(((account.getEquity()-account.getUsedMargin())/onlyOneOrderAmount ) / 1000, 3);
			} else {
				if(instruments.size() >= instrumentsFixed.length) {
                amount = round((((account.getEquity()-account.getUsedMargin()) / (amountOne*2)) / (instruments.size()*maxMerges*numOrders) ) / 1000, 3);
				} else amount = 0.001;
			}
            for (String key : ordersBusyTP.keySet()) {
                if(ordersBusyTP.get(key) < getTimeMillis()-10000/timeRate) 
                    ordersBusyTP.remove(key);
            }
            for (String key :  ordersBusySL.keySet()) {
                if(ordersBusySL.get(key) < getTimeMillis()-10000/timeRate) 
                    ordersBusySL.remove(key);
            }
        } catch (Exception e) {            
           //console.getErr().println("onAccount E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }
        
        if(Double.compare(amount, 0.001) < 0) amount = 0.001; 
		
		try {
            if((onAccountEmailSendTime < System.currentTimeMillis() - 60 * 60 * 1000)  || onAccountLatestsOrders.length() > 0 || onAccountCloseOnProfit.length() > 0) {
				
				if(onAccountEmailHour == Integer.parseInt(new SimpleDateFormat("HH").format(new Date())))
					onAccountEmailHourCount++;
				else onAccountEmailHourCount=1;
				
				if(onAccountEmailHourCount <= 100 && onAccountEmailSendTime < System.currentTimeMillis() - 1000) {
					onAccountEmailSendTime = System.currentTimeMillis();
					sendMail();
				}
			}
		} catch (Exception e) {            
           //console.getErr().println("onAccount E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }
		
        ordersExecutionLastRunCheck = false;
    }
        
    
    private void setInstuments (){
        int numInst = 1;
        try {
			if(onlyOneOrder) {
				numInst = instrumentsFixed.length;
			} else {
				IAccount accountSetInstuments = context.getAccount();
				numInst = (int) round((((accountSetInstuments.getEquity()-accountSetInstuments.getUsedMargin()) / (amountOne*2))) / (maxMerges*numOrders), 0);
			}
        } catch (Exception e) {
            //console.getErr().println("setInstrument numInst E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }
        try {
            if(numInst < 1) numInst =1;
            if(instruments.size() >= numInst) return;
            instruments = new ArrayList<>();
            int i = 1;
            for( Instrument myInstrument : instrumentsFixed  ) {
                if(i<=numInst){
                    instruments.add(myInstrument);
                }
                i++;
            }       
        } catch (Exception e2) {
            //console.getErr().println("setInstrument E: "+e2.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e2);
        }
    }
    
    public void closeOrdes(String closeType) {    
        try {
                    for( IOrder order : engine.getOrders()) {    
                        if(order.getState() == IOrder.State.FILLED || order.getState() == IOrder.State.OPENED){
                            if (order.getOrderCommand() == OrderCommand.SELL || order.getOrderCommand() == OrderCommand.BUY) {
                                ordersBusyTP.put(order.getId(),getTimeMillis()+1200/timeRate);  
                                ordersBusySL.put(order.getId(),getTimeMillis()+1200/timeRate); 
                            }
                        }
                    }
                    //Thread.sleep(10000);   
                    
					double closePip = onProfitClosePips;
					String closeOnProfitMsg = "";
                    for( IOrder order : engine.getOrders()) {
						if(!order.getLabel().contains("Signal:") || (order.getLabel().contains("Signal:") && manageSignals)) {                    
							if(order.getState() == IOrder.State.FILLED || order.getState() == IOrder.State.OPENED){
								OrderCommand cmd = order.getOrderCommand();
								if (cmd == OrderCommand.SELL || cmd == OrderCommand.BUY) {
									Instrument inst = order.getInstrument();
									double instumrntPipValue = inst.getPipValue();
									if(closeType.equals("dayEnd")) {
										closePip = round(getStdDev(inst)/instumrntPipValue*ProfitCloseDayEndMultiStdDev,1);
										if(Double.compare(onProfitClosePipsDayEnd, closePip) > 0) closePip = onProfitClosePipsDayEnd;
									} else if(closeType.equals("constant")) {
										closePip = round(getStdDev(inst)/instumrntPipValue*ProfitCloseMultiStdDev,1);
										if(Double.compare(onProfitClosePips, closePip) > 0) closePip = onProfitClosePips;
									}
									//console.getErr().println("closePip : "+closePip+ " setPipsDistance: " +setPipsDistance(inst,OrderCommand.PLACE_OFFER)+ " inst: " +inst.toString());
									if(Double.compare(order.getProfitLossInPips(),-closePip) < 0 || closeType.equals("CloseAll")) {
										order.close();
										closeOnProfitMsg += inst.toString()+" "+cmd+" "+order.getProfitLossInPips()+newLine;
										console.getInfo().println("ORDER-CLOSE closePip : "+closePip+ " pipsDist: " +setPipsDistance(inst,((cmd==OrderCommand.BUY)?OfferSide.ASK:OfferSide.BID))+ " inst: " +inst.toString() +"dir: "+cmd);
									}
								}
							}
						}
                    }
					if(closeOnProfitMsg.length() > 0)
						onAccountCloseOnProfit = closeType+newLine+closeOnProfitMsg;
        } catch (Exception e) {            
           //console.getErr().println("closeOrdes E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }    
    }

    public double getProfitOfOrdersAtProfit() {     
        double profitOfOrdersAtProfit =0;
        try {
            for( IOrder order : engine.getOrders()) {                  
                if(order.getState() == IOrder.State.FILLED || order.getState() == IOrder.State.OPENED){
                    if (order.getOrderCommand() == OrderCommand.SELL || order.getOrderCommand() == OrderCommand.BUY) {
                        if(Double.compare(order.getProfitLossInPips(),0) > 0) {
							
                            double priceWithoutSL = order.getProfitLossInPips();
                            Instrument inst = order.getInstrument();
                            double instumrntPipValue = inst.getPipValue();
                            double openPrice= order.getOpenPrice();
							
							double pipValueToAccountCurrency = order.getProfitLossInUSD()/order.getProfitLossInPips();
							
							
                            if(Double.compare(order.getStopLossPrice(),0) != 0) {
                                if(order.getOrderCommand() == OrderCommand.BUY ) {
                                    if(openPrice < order.getStopLossPrice())
                                        priceWithoutSL = (history.getLastTick(inst).getAsk()-order.getStopLossPrice())/instumrntPipValue;
                                } else if (order.getOrderCommand() == OrderCommand.SELL) {
                                    if(openPrice > order.getStopLossPrice())
                                        priceWithoutSL = (order.getStopLossPrice()-history.getLastTick(inst).getBid())/instumrntPipValue;
                                }
                            }
							
                            if(Double.compare(priceWithoutSL,0) > 0)
                                profitOfOrdersAtProfit += priceWithoutSL*pipValueToAccountCurrency;
                            
                        }
                    }
                }
            }
        
        } catch (Exception e) {            
           //console.getErr().println("getOrdersAtProfit E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }
        return profitOfOrdersAtProfit;
    }
    
    public void closeOrdesOnProfit(IAccount account) {
        try {
            Date now = new Date();
             
            double profitOfOrdersAtProfit = getProfitOfOrdersAtProfit();
			double accEQnotSettled = account.getEquity();
			if(Double.isNaN(accEQnotSettled)) return;
			
            double accEQ = accEQnotSettled-profitOfOrdersAtProfit;
            if(Double.compare(accEQ,gainBase*gainPercentage) > 0) {
                console.getOut().println("closeOrdesOnProfit accEQ > gainBase *"+gainPercentage+ " gainBase: " +gainBase + " accEQ: "+accEQ);       
                console.getOut().println( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(now));
                closeOrdes("constant");            
                            
                profitOfOrdersAtProfit = getProfitOfOrdersAtProfit();
                accEQ = account.getEquity()-profitOfOrdersAtProfit;
                if(Double.compare(accEQ,gainBase) > 0 && !Double.isNaN(accEQ)) {
                    gainBase = accEQ;
					setGainBase();
                }
			}else if(Double.compare(accEQnotSettled,gainBase*gainPercentageCloseAll) > 0) {
                console.getOut().println("closeOrdesOnProfit accEQ > gainBase *"+gainPercentageCloseAll+ " gainBase: " +gainBase + " accEQ: "+accEQnotSettled);       
                console.getOut().println( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(now));
                closeOrdes("CloseAll");            
                            
                profitOfOrdersAtProfit = getProfitOfOrdersAtProfit();
                accEQ = account.getEquity()-profitOfOrdersAtProfit;
                if(Double.compare(accEQ,gainBase) > 0 && !Double.isNaN(accEQ)) {
                    gainBase = accEQ;
					setGainBase();
                }
			
            } else if(Double.compare(accEQ,gainBase*gainEndOfDayPercentage) > 0 && Integer.parseInt(new SimpleDateFormat("HH").format(now))+1 == 23 && Integer.parseInt(new SimpleDateFormat("mm").format(now)) >= 49 && !dayEndClose) {
                dayEndClose = true;
                console.getOut().println( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(now));
                console.getOut().println("closeOrdesOnProfit dayEnd accEQ > gainBase *"+gainEndOfDayPercentage+"  gainBase: " +gainBase + "*" +gainEndOfDayPercentage+ " accEQ: "+accEQ);
                closeOrdes("dayEnd");          
                profitOfOrdersAtProfit = getProfitOfOrdersAtProfit();
                accEQ = account.getEquity()-profitOfOrdersAtProfit;
				
                if(Double.compare(accEQ,gainBase) > 0 && !Double.isNaN(accEQ)) {
                    gainBase = accEQ;
					setGainBase();
                }
            }
            if(Integer.parseInt(new SimpleDateFormat("HH").format(now))+1 >= 0 && Integer.parseInt(new SimpleDateFormat("mm").format(now)) >= 0 && dayEndClose) 
                dayEndClose = false;
            

            
        } catch (Exception e) {            
           //console.getErr().println("onAccount accEQ E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }
        
    }
    
    public double getAmount(Instrument instrument) {
        return amount;
    }


    
    public double getAmountByAccountCurrency(Instrument instrument) {
		try {
			if(accountCurrency.equals(instrument.getSecondaryJFCurrency()))
				return amount;
		
			String crossInst = accountCurrency + instrument.getPairsSeparator() + instrument.getSecondaryJFCurrency(); 
			if(!Instrument.isInverted(crossInst))
				return round(amount*history.getLastTick(Instrument.fromString(crossInst)).getAsk(),3);
		
			crossInst =  instrument.getSecondaryJFCurrency() + instrument.getPairsSeparator() + accountCurrency; 
			return round(amount/history.getLastTick(Instrument.fromString(crossInst)).getAsk(),3);
		
		} catch (Exception e) {            
           //console.getErr().println("onAccount accEQ E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }
		return amount;
    }
	
    public double getFirstStep(Instrument instrument) {
        double firstStep =  ((getStdDev(instrument)/(periodToMins(STDPeriod)))/1)*0.9;
        if(Double.compare(minFirstStep*instrument.getPipValue(),firstStep) > 0) firstStep = minFirstStep*instrument.getPipValue();
        return firstStep;
    }
    public double getMinStdDev(Instrument instrument) {
		double firstStep = getFirstStep(instrument)/instrument.getPipValue();
        //double minSetStdDev = (getStdDev(instrument)/(periodToMins(STDPeriod)/((firstStep)/(1+(1/(firstStep))))))*1;
		double minSetStdDev = firstStep*instrument.getPipValue()*firstStepSTDdevider*0.9; 
		//console.getErr().println("getStdDev:" +round(getStdDev(instrument),instrument.getPipScale()+1)+" minSetStdDev:" +round(minSetStdDev,instrument.getPipScale()+1)+" firstStep:" +round(firstStep,instrument.getPipScale()+1)+" " +instrument.toString());
		
        if(Double.compare(minStdDev*instrument.getPipValue(),minSetStdDev) > 0) return minStdDev*instrument.getPipValue();
        return minSetStdDev;
		
		//console.getErr().println("firstStep firstStep:" +round(firstStep,instrument.getPipScale()+1)+" rt:" +round((1+(1/(firstStep/instrument.getPipValue()))*5),instrument.getPipScale()+1)+" " +instrument.toString());
		//double firstStepRationed = firstStep*(1+(1/(firstStep/instrument.getPipValue())));
		
        //double minSetStdDev = getStdDev(instrument)/(periodToMins(STDPeriod)*firstStepRationed);
		
		
        // minSetStdDev = round((firstStepRationed/(getStdDev(instrument)/(periodToMins(STDPeriod)*STDminsPeriod)))/10*instrument.getPipValue(),instrument.getPipScale()+1);
		

    }


    
    public double getStdDev(Instrument instrument) {
		double mininalException = (minStdDev*periodToMins(STDPeriod))*instrument.getPipValue();
        try {
            if (instrumentsSTDdevAVG.containsKey(instrument.toString())) {
                if(instrumentsSTDdevTime.get(instrument.toString()) >= getTimeMillis()-30*60*1000) {
                    return instrumentsSTDdevAVG.get(instrument.toString());
                }
            }        
               
            if (!instrumentSTDdevBusy.containsKey(instrument.toString())) 
                instrumentSTDdevBusy.put(instrument.toString(), false);
			
            if(instrumentSTDdevBusy.get(instrument.toString()) == true) {
				if (instrumentsSTDdevAVG.containsKey(instrument.toString()))
                    return instrumentsSTDdevAVG.get(instrument.toString());
                else return mininalException;
            }   
            instrumentSTDdevBusy.put(instrument.toString(), true);
    
            double curSTDdev  = 0; 
            double maxBigFrame = indicators.max(instrument, STDPeriod, OfferSide.ASK, AppliedPrice.HIGH, STDminsPeriod, 0);
            double minBigFrame = indicators.min(instrument, STDPeriod, OfferSide.BID, AppliedPrice.LOW, STDminsPeriod, 0);
            
            curSTDdev = (maxBigFrame-minBigFrame);
                
            if(Double.compare(curSTDdev,0) <= 0)     
                curSTDdev = mininalException;
        
            if(Double.compare(curSTDdev,mininalException) != 0) {
                instrumentsSTDdevAVG.put(instrument.toString(),curSTDdev);
                instrumentsSTDdevTime.put(instrument.toString(),getTimeMillis());
                instrumentSTDdevBusy.put(instrument.toString(), false);
                return curSTDdev;
            }
        } catch (Exception e) {        
        }        
		
        instrumentSTDdevBusy.put(instrument.toString(), false);   
        return mininalException;
    }


    private Map<String, Double> getTimeFrames(Instrument instrument) {
        
		Map<String, Double> timeFrames =new HashMap<String, Double>();
		timeFrames.put("setTime",0.0);
		timeFrames.put("foundTime",0.0);
		 
        double setTime=0.0;
        try {
            if (instrumentsTimeFrames.containsKey(instrument.toString())) {
                if(instrumentsTimeFrameTime.get(instrument.toString()) >= getTimeMillis()-30000)
                    return instrumentsTimeFrames.get(instrument.toString());
            }
       
            if (!instrumentsTimeFrameBusy.containsKey(instrument.toString()))
                instrumentsTimeFrameBusy.put(instrument.toString(), false);

            if(instrumentsTimeFrameBusy.get(instrument.toString()) == true)
                return timeFrames;
            instrumentsTimeFrameBusy.put(instrument.toString(), true);
			
            double bigFrame = getStdDev(instrument);
            double oneMinByBigFrame =  bigFrame/(STDminsPeriod*periodToMins(STDPeriod));
            
            double minSetStdDev = getMinStdDev(instrument);
			
            double maxFifteen, minFifteen;
            double oneMinChange = 0;
            double oneMinChangeSet = 0;
        
            if(Double.compare(oneMinChangeSet,0) == 2) {
				try { 
					maxFifteen = indicators.max(instrument, Period.TEN_SECS, OfferSide.ASK, AppliedPrice.HIGH, 24, 0);
					minFifteen = indicators.min(instrument, Period.TEN_SECS, OfferSide.BID, AppliedPrice.LOW, 24, 0);
					oneMinChange = maxFifteen-minFifteen;
				
					if(Double.compare(oneMinChange,minSetStdDev) >= 0) {
						for(int i=2; i<=24; i++){
							if(Double.compare(oneMinChangeSet,0) == 0) {
								try { 
									maxFifteen = indicators.max(instrument, Period.TEN_SECS, OfferSide.ASK, AppliedPrice.HIGH, i, 0);
									minFifteen = indicators.min(instrument, Period.TEN_SECS, OfferSide.BID, AppliedPrice.LOW, i, 0);
									oneMinChange = maxFifteen-minFifteen;
                
									if(Double.compare(oneMinChange,minSetStdDev) >= 0) {
										oneMinChangeSet = oneMinChange/((i/6)*1);
										timeFrames.put("foundTime",(double)((i/6)*1));
									}
								} catch (Exception e) {
								}
							}
						}    
					}
				} catch (Exception e) {
				}
            }        
            if(Double.compare(oneMinChangeSet,0) == 0) {
				try { 
					if (instrumentsTimeFrameTime.containsKey(instrument.toString()+"_ONE_MIN")) {
						if(instrumentsTimeFrameTime.get(instrument.toString()+"_ONE_MIN") >= getTimeMillis()-30000)
							oneMinChangeSet = instrumentsTimeFrame.get(instrument.toString()+"_ONE_MIN");
					}
					if(Double.compare(oneMinChangeSet,0) == 0) {
						maxFifteen = indicators.max(instrument, Period.ONE_MIN, OfferSide.ASK, AppliedPrice.HIGH, 120, 0);
						minFifteen = indicators.min(instrument, Period.ONE_MIN, OfferSide.BID, AppliedPrice.LOW, 120, 0);
						oneMinChange = maxFifteen-minFifteen;
				
						if(Double.compare(oneMinChange,minSetStdDev) >= 0) {
							for(int i=2; i<=120; i++){// 30
								if(Double.compare(oneMinChangeSet,0) == 0) {
									try { 
										maxFifteen = indicators.max(instrument, Period.ONE_MIN, OfferSide.ASK, AppliedPrice.HIGH, i, 0);
										minFifteen = indicators.min(instrument, Period.ONE_MIN, OfferSide.BID, AppliedPrice.LOW, i, 0);
										oneMinChange = maxFifteen-minFifteen;
                
										if(Double.compare(oneMinChange,minSetStdDev) >= 0) {
											oneMinChangeSet = oneMinChange/(i*1);
											timeFrames.put("foundTime",(double)(i*1));
											instrumentsTimeFrame.put(instrument.toString()+"_ONE_MIN",oneMinChangeSet);
										}
									} catch (Exception e) {
									}
								}
							}
						}
					}
				} catch (Exception e) {
				}
            }
            
            if(Double.compare(oneMinChangeSet,0) == 0) {
				try{
					if (instrumentsTimeFrameTime.containsKey(instrument.toString()+"_FIVE_MINS")) {
						if(instrumentsTimeFrameTime.get(instrument.toString()+"_FIVE_MINS") >= getTimeMillis()-2.5*60*1000)
							oneMinChangeSet = instrumentsTimeFrame.get(instrument.toString()+"_FIVE_MINS");
					}
				
					if(Double.compare(oneMinChangeSet,0) == 0) {
						maxFifteen = indicators.max(instrument, Period.FIVE_MINS, OfferSide.ASK, AppliedPrice.HIGH, 48, 0);
						minFifteen = indicators.min(instrument, Period.FIVE_MINS, OfferSide.BID, AppliedPrice.LOW, 48, 0);
						oneMinChange = maxFifteen-minFifteen;
				
						if(Double.compare(oneMinChange,minSetStdDev) >= 0) {
							for(int i=24; i<=48; i++){
								if(Double.compare(oneMinChangeSet,0) == 0) {
									try { 
										maxFifteen = indicators.max(instrument, Period.FIVE_MINS, OfferSide.ASK, AppliedPrice.HIGH, i, 0);
										minFifteen = indicators.min(instrument, Period.FIVE_MINS, OfferSide.BID, AppliedPrice.LOW, i, 0);
										oneMinChange = maxFifteen-minFifteen;
                
										if(Double.compare(oneMinChange,minSetStdDev) >= 0) {
											oneMinChangeSet = oneMinChange/(i*5);
											timeFrames.put("foundTime",(double)(i*5));
											instrumentsTimeFrame.put(instrument.toString()+"_FIVE_MINS",oneMinChangeSet);
										}
									} catch (Exception e) {
									}
								}
							}    
						}
					}
				} catch (Exception e) {
				}
            }
			
            if(Double.compare(oneMinChangeSet,0) == 0) {
				try{
					if (instrumentsTimeFrameTime.containsKey(instrument.toString()+"_FIFTEEN_MINS")) {
						if(instrumentsTimeFrameTime.get(instrument.toString()+"_FIFTEEN_MINS") >= getTimeMillis()-7.5*60*1000)
							oneMinChangeSet = instrumentsTimeFrame.get(instrument.toString()+"_FIFTEEN_MINS");
					}
					if(Double.compare(oneMinChangeSet,0) == 0) {
						maxFifteen = indicators.max(instrument, Period.FIFTEEN_MINS, OfferSide.ASK, AppliedPrice.HIGH, 192, 0);
						minFifteen = indicators.min(instrument, Period.FIFTEEN_MINS, OfferSide.BID, AppliedPrice.LOW, 192, 0);
						oneMinChange = maxFifteen-minFifteen;
				
						if(Double.compare(oneMinChange,minSetStdDev) >= 0) {
							for(int i=16; i<=192; i++){
								if(Double.compare(oneMinChangeSet,0) == 0) {
									try { 
										maxFifteen = indicators.max(instrument, Period.FIFTEEN_MINS, OfferSide.ASK, AppliedPrice.HIGH, i, 0);
										minFifteen = indicators.min(instrument, Period.FIFTEEN_MINS, OfferSide.BID, AppliedPrice.LOW, i, 0);
										oneMinChange = maxFifteen-minFifteen;
							
										if(Double.compare(oneMinChange,minSetStdDev) >= 0) {
											oneMinChangeSet = oneMinChange/(i*15);
											timeFrames.put("foundTime",(double)(i*15));
											instrumentsTimeFrame.put(instrument.toString()+"_FIFTEEN_MINS",oneMinChangeSet);
										}
									} catch (Exception e) {
									}
								}
							}
						}
					}
				} catch (Exception e) {
				}
            }
            
            if(Double.compare(oneMinChangeSet,0) == 0) {
                instrumentsTimeFrameBusy.put(instrument.toString(), false);
                return timeFrames;
            }
            
          
            Double changeBigFrameToOneMin = oneMinChangeSet/oneMinByBigFrame;
            setTime = (changeBigFrameToOneMin.intValue());
			timeFrames.put("setTime",(double)(setTime));
			//timeFrames.put("changeBigFrameToOneMin",(double)(changeBigFrameToOneMin));
        } catch (Exception e) {
            setTime=0.0;
        }
        if(Double.compare(setTime,0) > 0) {
            instrumentsTimeFrames.put(instrument.toString(),timeFrames);
            instrumentsTimeFrameTime.put(instrument.toString(),getTimeMillis());
        }
		
        instrumentsTimeFrameBusy.put(instrument.toString(), false);
        return timeFrames;
    }
    
    
    
    public double setPipsDistance(Instrument instrument, OfferSide side) {

        String direction= (side == OfferSide.ASK) ? "UP" : "DW";
        double curSTDdev  = 0; 
        try {
			
            Map<String, Double> setTimeFrames = getTimeFrames(instrument);
            double setTimeFrame = (double) setTimeFrames.get("setTime");
            double minSetStdDev = getMinStdDev(instrument);

            if(Double.compare(setTimeFrame,0) == 0) return minSetStdDev;
            
            if (instrumentsSTDdev.containsKey(instrument.toString()+"_"+direction)) {
                if(instrumentsSTDdevTime.get(instrument.toString()+"_"+direction) >= getTimeMillis()-30000) 
                    return instrumentsSTDdev.get(instrument.toString()+"_"+direction);
            }        
       
            if (!instrumentSTDdevBusy.containsKey(instrument.toString()+"_"+direction)) 
                instrumentSTDdevBusy.put(instrument.toString()+"_"+direction, false);

            if(instrumentSTDdevBusy.get(instrument.toString()+"_"+direction) == true) 
                return minSetStdDev;

			
            instrumentSTDdevBusy.put(instrument.toString()+"_"+direction, true);
            try{
                double  max = indicators.max(instrument, minsToPeriod(setTimeFrame), side, AppliedPrice.HIGH, minsToPeriodScale(setTimeFrame), 0);
                double  min = indicators.min(instrument, minsToPeriod(setTimeFrame), side, AppliedPrice.LOW, minsToPeriodScale(setTimeFrame), 0);
                curSTDdev = (max-min);
                
                if (Double.compare(curSTDdev,minSetStdDev) > 0) {
                    instrumentsSTDdevTime.put(instrument.toString()+"_"+direction, getTimeMillis());
                    instrumentsSTDdev.put(instrument.toString()+"_"+direction, curSTDdev);
                }
                if(Double.compare(curSTDdev,0) <= 0)     
                    curSTDdev = minSetStdDev;    
				
            } catch (Exception e) {
                curSTDdev = minSetStdDev;            
            }        
        
            if (Double.compare(curSTDdev,0) == 0 || Double.compare(curSTDdev,minSetStdDev) < 0) {
                curSTDdev = minSetStdDev;
            }
        } catch (Exception e) {            
            //console.getErr().println("setPipsDistance call() E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e +" " +instrument.toString());
        }
        instrumentSTDdevBusy.put(instrument.toString()+"_"+direction, false);
        return curSTDdev;
    }
     
    
    
    
    
    
    
    
    
    
    
        
            
    private String getTrend(Instrument instrument, OfferSide side) {
        
        String trend = "NotDefined";
		
        try{       
            if (!instrumentDirectionBusy.containsKey(instrument.toString())) instrumentDirectionBusy.put(instrument.toString(), false);
            if(instrumentDirectionBusy.get(instrument.toString()) == true) return trend;
            instrumentDirectionBusy.put(instrument.toString(), true);
               
        } catch (Exception e) {}
        
		
        try{
			
			
            Map<String, Double> setTimeFrames = getTimeFrames(instrument);
            double timeFrame = (double) setTimeFrames.get("setTime");
            if(Double.compare(timeFrame,0) == 0) return trend;
			
            //double changeBigFrameToOneMin = (double) setTimeFrames.get("changeBigFrameToOneMin");
            //double currentMinimalDistance = getMinStdDev(instrument)/changeBigFrameToOneMin;
			//console.getErr().println("getTrend currentMinimalDistance: "+currentMinimalDistance+" " +instrument.toString());
            //double pipDistanceSet = setPipsDistance(instrument, side);
            //double currentRateAmin = pipDistanceSet/timeFrame;
			//console.getErr().println("getTrend currentRateAmin: "+currentRateAmin+" " +instrument.toString());
			//console.getErr().println("getTrend currentRateAmin: "+currentRateAmin+" " +instrument.toString());
			//console.getErr().println("getTrend minimal pip: "+currentMinimalDistance/currentRateAmin+" pip: "+pipDistanceSet+" " +instrument.toString());
			//if(currentMinimalDistance/currentRateAmin > pipDistanceSet) return  trend;
			
			
			//console.getErr().println("getTrend timeFrame: "+timeFrame+" " +instrument.toString());
			String priceDifference,MA,CCI;
			
			if(side == OfferSide.ASK) {
				instrumentsTrendInfo.put(instrument.toString()+"_UP","tf:"+round(timeFrame,1)+""+newLine+"");
				CCI = getCCI(instrument, timeFrame, OfferSide.ASK);
				if(CCI.equals("UP")) {
					priceDifference = getPriceDifference(instrument, OfferSide.ASK, setTimeFrames);
					if(priceDifference.equals("UP")) {
						MA = getMA(instrument, timeFrame, OfferSide.ASK);
						if(MA.equals("UP")) {
							trend = "UP";
						}
					}
				}
				if(trend.equals("NotDefined"))
					instrumentsTrendInfo.put(instrument.toString()+"_UP","");
				instrumentDirectionBusy.put(instrument.toString(), false);
				return trend;
			}
			
			
			if(side == OfferSide.BID) {
				instrumentsTrendInfo.put(instrument.toString()+"_DW","tf:"+round(timeFrame,1)+""+newLine+"");
				CCI = getCCI(instrument, timeFrame, OfferSide.BID);
				if(CCI.equals("DW")) {
					priceDifference = getPriceDifference(instrument, OfferSide.BID, setTimeFrames);
					if(priceDifference.equals("DW")) {
						MA = getMA(instrument, timeFrame, OfferSide.BID);
						if(MA.equals("DW")) {
							trend = "DW";
						}
					}
				}
				if(trend.equals("NotDefined"))
					instrumentsTrendInfo.put(instrument.toString()+"_DW","");
				instrumentDirectionBusy.put(instrument.toString(), false);
				return trend;
			}
       
        } catch (Exception e) {
            //console.getErr().println("getTrend FIFTEEN_MINS E: "+e.getMessage()+" " +instrument.toString());
        }
        return trend;
    }
    
    public String getCCI(Instrument instrument, double timePeriod, OfferSide side) {
        String trend = "NotDefined";
		if(withoutCCI)  return (side == OfferSide.BID) ? "DW" : "UP";
        try{ 
			double[] cci;   
			
            double timePeriodCCI = timePeriod*8;
            long to = history.getBarStart(minsToPeriod(timePeriod), history.getLastTick(instrument).getTime());
            long from = to-minsToPeriod(timePeriod).getInterval();
                 
            cci = indicators.cci(instrument, minsToPeriod(timePeriod) , side, minsToPeriodScale((timePeriod)), Filter.WEEKENDS, from, to);   
			//console.getErr().println("getCCI minsToPeriod: "+minsToPeriod(timePeriodCCI)+" minsToPeriodScale: "+minsToPeriodScale((timePeriodCCI))+" " +instrument.toString());         
            if(Double.compare(cci[0],cci[1]) > 0 && Double.compare(cci[1],500) < 0 && Double.compare(cci[1],120) > 0)
                trend = "DW";
            else if(Double.compare(cci[0],cci[1]) < 0 && Double.compare(cci[1],-500) > 0 && Double.compare(cci[1],-120) < 0)
                trend = "UP";
				
            if(!trend.equals("NotDefined") &&  (((side == OfferSide.BID)?"DW":"UP") == trend))
				instrumentsTrendInfo.put(instrument.toString()+"_"+trend, instrumentsTrendInfo.get(instrument.toString()+"_"+trend)+"CCI: "+round(cci[0],0)+""+(trend.equals("UP")?"<":">")+""+round(cci[1],0)+" "+minsToPeriod(timePeriodCCI).toString()+":"+minsToPeriodScale((timePeriodCCI))+""+newLine+"");
                
        } catch (Exception e) {
            //console.getErr().println("getTrend cci E: "+e.getMessage()+" " +instrument.toString());
        }
		 
        //trend= (side == OfferSide.BID) ? "DW" : "UP";
        return trend;
    }
    
    public String getMA(Instrument instrument, double timePeriod, OfferSide side) {
        String trend = "NotDefined";
		if(withoutMA)  return (side == OfferSide.BID) ? "DW" : "UP";
        try{
            int instrumentPipScale = instrument.getPipScale();
			double lastTickPrice = (side == OfferSide.BID) ? history.getLastTick(instrument).getBid() : history.getLastTick(instrument).getAsk();
			AppliedPrice appliedPrice = (side == OfferSide.BID) ? AppliedPrice.HIGH : AppliedPrice.LOW;
			
            int numBars;
			double timePeriodMA;
            long to,from;
			double[] ma;
			
  				
			numBars = 1;
			timePeriodMA = timePeriod*2;
            to = history.getBarStart(minsToPeriod(timePeriodMA), history.getLastTick(instrument).getTime());
            from = to-minsToPeriod(timePeriodMA).getInterval()*numBars;
			
			
			ma = indicators.kama(instrument, minsToPeriod(timePeriodMA), side, appliedPrice, (int)minsToPeriodScale(timePeriodMA), (int)minsToPeriodScale(timePeriodMA)/4, (int)minsToPeriodScale(timePeriodMA)/2, Filter.WEEKENDS, from, to);
			
            //ma = indicators.sma(instrument, minsToPeriod(timePeriodMA) , side, appliedPrice, minsToPeriodScale((timePeriodMA)), Filter.WEEKENDS, from, to);            
			//console.getErr().println("getMA minsToPeriod: "+minsToPeriod(timePeriodMA)+" timePeriod: "+timePeriod+" " +instrument.toString());
			if(Double.compare(ma[0],ma[numBars]) > 0) 
                trend = "DW";
            else if(Double.compare(ma[0],ma[numBars]) < 0)
                trend = "UP";
			else 
				trend = "NotDefined";
			
			if(trend.equals("NotDefined"))  return trend;
			instrumentsTrendInfo.put(instrument.toString()+"_"+trend,  instrumentsTrendInfo.get(instrument.toString()+"_"+trend)+"sma:"+round(ma[0],instrumentPipScale+1)+""+(trend.equals("DW")?">":"<")+""+round(ma[numBars],instrumentPipScale+1)+" "+minsToPeriod(timePeriodMA).toString()+":"+minsToPeriodScale((timePeriodMA))+""+newLine+"");
			
			if(!trend.equals("NotDefined"))  return trend;
			
			//////
            numBars = 2;
			timePeriodMA = timePeriod/60;
            to = history.getBarStart(minsToPeriod(timePeriodMA), history.getLastTick(instrument).getTime());
            from = to-minsToPeriod(timePeriodMA).getInterval()*numBars;
			
            ma = indicators.sma(instrument, minsToPeriod(timePeriodMA) , side, appliedPrice, minsToPeriodScale((timePeriodMA)), Filter.WEEKENDS, from, to);            
			//console.getErr().println("getMA minsToPeriod: "+minsToPeriod(timePeriodMA)+" minsToPeriodScale: "+minsToPeriodScale((timePeriodMA))+" " +instrument.toString());
			if(trend.equals("DW") && Double.compare(ma[0],ma[numBars]) > 0 && Double.compare(lastTickPrice,ma[numBars]) < 0) 
                trend = "DW";
            else if(trend.equals("UP") && Double.compare(ma[0],ma[numBars]) < 0 && Double.compare(lastTickPrice,ma[numBars]) > 0)
                trend = "UP";
			else 
				trend = "NotDefined";
			
			if(trend.equals("NotDefined"))  return trend;
			instrumentsTrendInfo.put(instrument.toString()+"_"+trend,  instrumentsTrendInfo.get(instrument.toString()+"_"+trend)+"sma2:"+round(ma[0],instrumentPipScale+1)+""+(trend.equals("DW")?">":"<")+""+round(ma[numBars],instrumentPipScale+1)+" "+minsToPeriod(timePeriodMA).toString()+":"+minsToPeriodScale((timePeriodMA))+""+newLine+"");
			
			

			if(!trend.equals("NotDefined"))  return trend;
        } catch (Exception e) {
            //console.getErr().println("getTrend ma E: "+e.getMessage()+" " +instrument.toString());
        }
        //trend= (side == OfferSide.BID) ? "DW" : "UP";
        return trend;
    }
    
    
    public String getPriceDifference(Instrument instrument, OfferSide side, Map<String, Double> setTimeFrames) {
        double timeFrame = (double) setTimeFrames.get("setTime");
        String trend = "NotDefined";
		if(withoutPriceDifference)  return (side == OfferSide.BID) ? "DW" : "UP";
        try{
            int instrumentPipScale = instrument.getPipScale();
            Long timeOfLastTick= history.getTimeOfLastTick(instrument);
			AppliedPrice appliedPrice = (side == OfferSide.BID) ? AppliedPrice.HIGH : AppliedPrice.LOW;
			double lastTickPrice = (side == OfferSide.BID) ? history.getLastTick(instrument).getBid() : history.getLastTick(instrument).getAsk();
			
            double pipDistanceSet = setPipsDistance(instrument, side);


			double timePeriodMA = timeFrame;//(double) setTimeFrames.get("foundTime")*1;

            double pipDistanceRequiredBid = pipDistanceSet;
            double pipDistanceRequiredAsk = pipDistanceSet;
			
			if(side == OfferSide.BID) {
                double min = indicators.min(instrument, minsToPeriod(timePeriodMA), side, AppliedPrice.LOW, minsToPeriodScale(timePeriodMA), 0);
				//console.getErr().println("getTrend lastTickPrice-min: "+(lastTickPrice-min)+" " +instrument.toString());
				if(Double.compare(lastTickPrice-min,pipDistanceRequiredBid*0.9) >= 0) {
					trend =  "DW";
					instrumentsTrendInfo.put(instrument.toString()+"_"+trend,  instrumentsTrendInfo.get(instrument.toString()+"_"+trend)+" pipDif:"+round(lastTickPrice-min,instrumentPipScale+1)+""+newLine+"pipDis:"+round(pipDistanceRequiredBid,instrumentPipScale+1)+" "+minsToPeriod(timePeriodMA).toString()+":"+minsToPeriodScale((timePeriodMA))+""+newLine);
				} 
			} else if(side == OfferSide.ASK){
                double max = indicators.max(instrument, minsToPeriod(timePeriodMA), side, AppliedPrice.HIGH, minsToPeriodScale(timePeriodMA), 0);
				//console.getErr().println("getTrend max-lastTickPrice: "+(max-lastTickPrice)+" " +instrument.toString());
				if(Double.compare(max-lastTickPrice,pipDistanceRequiredAsk*0.9) >= 0) {
					trend =  "UP";
					instrumentsTrendInfo.put(instrument.toString()+"_"+trend,  instrumentsTrendInfo.get(instrument.toString()+"_"+trend)+" pipDif:"+round(max-lastTickPrice,instrumentPipScale+1)+""+newLine+"pipDis:"+round(pipDistanceRequiredBid,instrumentPipScale+1)+" "+minsToPeriod(timePeriodMA).toString()+":"+minsToPeriodScale((timePeriodMA))+""+newLine);
				}
			}

        } catch (Exception e) {
            //console.getErr().println("getTrend ma E: "+e.getMessage()+" " +instrument.toString());
        }
        //trend= (side == OfferSide.BID) ? "DW" : "UP";
        return trend;
    }   

    
    
    
    
    
    
    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
    }    
    
    @Override
    public void onStop()  {
		try {
			if(!emailReports) return;
			context.getUtils().sendMail("kashirin.alex@gmail.com", reportName+" stopped", "strategy stopped");
        } catch (Exception e) {
           //console.getErr().println("onMessage E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }  
    }
        
    @Override
    public void onMessage(IMessage message) {   

      try {      
  
        switch(message.getType()){
            case ORDER_SUBMIT_OK : 
                print("Order opened: " + message.getOrder());
                break;
            case ORDER_SUBMIT_REJECTED : 
                //print("Order open failed: " + message.getOrder());
                break;
            case ORDER_FILL_OK : 
                print("Order filled: " + message.getOrder());
                break;
            case ORDER_FILL_REJECTED : 
                //print("Order cancelled: " + message.getOrder());
                break;
            case ORDER_CLOSE_OK : 
				IOrder order = message.getOrder();
				OrderCommand cmd = order.getOrderCommand();
				
				if(Double.compare(order.getProfitLossInPips()*order.getInstrument().getPipValue(),getFirstStep(order.getInstrument())*distanceAsProfitable) >= 0) {// getFirstStep(order.getInstrument())
					
					boolean buyOrder = true;
					boolean sellOrder =true;
					for( IOrder o : engine.getOrders(order.getInstrument())) {
						if (o.getOrderCommand() == OrderCommand.BUY) buyOrder= false;
						else if(o.getOrderCommand() == OrderCommand.SELL)  sellOrder= false;
					}
					if((cmd==OrderCommand.SELL && buyOrder) || (cmd==OrderCommand.BUY && sellOrder)) {
					//Map<String, Double> setTimeFrames = getTimeFrames(order.getInstrument());
					//double timeFrame = (double) setTimeFrames.get("setTime");
					//if(Double.compare(timeFrame,0) != 0) {
			
					//	String MA = getMA(order.getInstrument(),timeFrame,((cmd==OrderCommand.BUY)?OfferSide.BID:OfferSide.ASK));
					//	if(MA.equals(((cmd==OrderCommand.BUY)?"DW":"UP"))) {
							instrumentsTrendInfo.put(order.getInstrument().toString()+"_"+((cmd==OrderCommand.BUY)?"DW":"UP"),  "OrderByOpoSide prev "+((cmd==OrderCommand.BUY)?"UP":"DW")+" P/L:"+order.getProfitLossInPips()+"pip"+newLine);
							submitOrder(order.getInstrument(), getAmount(order.getInstrument()), ((cmd==OrderCommand.BUY)?OrderCommand.SELL:OrderCommand.BUY));
							
							//console.getErr().println("onMessage submitOrder: "+((cmd==OrderCommand.BUY)?"DW":"UP")+" " +order.getInstrument().toString());
					//	}
					//}
					}
				}
                //
                //if(order.getProfitLossInPips()!=0)
                    //print("Order closed: " + message.getOrder()+ " with P/L "+order.getProfitLossInPips()+" pip");
                break;
            }
        } catch (Exception e) {
           //console.getErr().println("onMessage E: "+e.getMessage()+" Thread: " + Thread.currentThread().getName() + " " + e);
        }      
    }

    

	 public int periodToMins(Period period){
        if(period == Period.ONE_HOUR) return (int) 60;
        else if(period == Period.DAILY) return (int) 60*24;
        else if(period == Period.FOUR_HOURS) return (int) 60*4;
        else if(period == Period.THIRTY_MINS) return (int) 30;
        else if(period == Period.FIFTEEN_MINS ) return (int) 15;
        else if(period == Period.WEEKLY ) return (int) 60*24*7;
    	return (int) 2;
    }
    
    public int minsToPeriodScale(double timePeriod){
		if(Double.compare(timePeriod*6,1) <= 0)  timePeriod = 2;
		
        if(Double.compare(timePeriod,2) <= 0)		return (int) (timePeriod*6);
        if(Double.compare(timePeriod,360) <= 0)  	return (int) timePeriod;
        if(Double.compare(timePeriod,1800) <= 0) 	return (int)(timePeriod/5);
        if(Double.compare(timePeriod,3600) <= 0) 	return (int)(timePeriod/10);
        if(Double.compare(timePeriod,5400) <= 0) 	return (int)(timePeriod/15);
        if(Double.compare(timePeriod,10800) <= 0) 	return (int)(timePeriod/30);
        if(Double.compare(timePeriod,21600) <= 0) 	return (int)(timePeriod/60);
        if(Double.compare(timePeriod,86400) <= 0) 	return (int)(timePeriod/360);
        if(Double.compare(timePeriod,518400) <= 0) 	return (int)(timePeriod/1440);
        if(Double.compare(timePeriod,3628800) <= 0) return (int)(timePeriod/10080);
		
		return (int) timePeriod;
    }
    
    public Period minsToPeriod(double timePeriod){
        if(Double.compare(timePeriod,2) <= 0) return Period.TEN_SECS;
        if(Double.compare(timePeriod,360) <= 0) return Period.ONE_MIN;
        if(Double.compare(timePeriod,1800) <= 0) return Period.FIVE_MINS;
        if(Double.compare(timePeriod,3600) <= 0) return Period.TEN_MINS;
        if(Double.compare(timePeriod,5400) <= 0) return Period.FIFTEEN_MINS;
        if(Double.compare(timePeriod,10800) <= 0) return Period.THIRTY_MINS;
        if(Double.compare(timePeriod,21600) <= 0) return Period.ONE_HOUR;
        if(Double.compare(timePeriod,86400) <= 0) return Period.FOUR_HOURS;
        if(Double.compare(timePeriod,518400) <= 0) return Period.DAILY;
        if(Double.compare(timePeriod,3628800) <= 0) return Period.WEEKLY;
        return Period.FIFTEEN_MINS;
    }
    
    public void sendMail() {
		try {
			if(!emailReports) return;
			String content = "";
			
			IAccount account = context.getAccount();
			
			content += ""+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())+newLine;
			content += "Eq: "+account.getEquity()+newLine;
			content += "Open: "+engine.getOrders().size()+newLine;
			if(onAccountCloseOnProfit.length() > 0){
				content += "Close On:"+newLine+onAccountCloseOnProfit+newLine;
				onAccountCloseOnProfit = "";
			}
			if(onAccountLatestsOrders.length() > 0 && content.length()+onAccountLatestsOrders.length() <= 500){
				content += "Latest:"+onAccountLatestsOrders+newLine;
				onAccountLatestsOrders = "";
			}
			if(content.length() >500) content = content.substring(0,500);
			
			//console.getInfo().println("sendMail: "+content.length());
			context.getUtils().sendMail("kashirin.alex@gmail.com", reportName+" acc status", content);

		} catch (Exception e) {
			console.getErr().println("sendMail E: "+e.getMessage());
		}
	}
    
    
    private void print (Object o){
        //console.getOut().println(o);
    }
    private static double round(double amount, int decimalPlaces) {
    return (new BigDecimal(amount)).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}