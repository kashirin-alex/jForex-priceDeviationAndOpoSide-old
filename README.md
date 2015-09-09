# jForex-priceDeviationAndOpoSide

What is that strategy:

MANAGE ACCOUNT:

* set the tradable instruments by equity 
* set the amount for the orders by equity 
* close order as the current equity is above gain-percentage
* enter SL as trailing step at profit
* email on new orders
* email on orders close by gainBase

the amount is calculated by the FreeMargin/numOrders/2/ValuePerPip (like, 4USD per pip of one order of instrument , 100Pip=400USD equity usage)

ORDERS EXECUTION:

 1) price-deviation,  as the price has the change by the current timePeriodSet MinMax %90-/+
price-deviation is (currentFoundBaseMinMaxToOneMin/changeBigFrameToOneMinTheWeekMinMax) = equal current probability for timePeriod to work with. -> timePeriodSet
 
 2) CCI 
 +120 it is sell, CCI is -120 it is buy (timeperiod by timePeriodSet x8 )
 
 3) kama 
(kamaTimeperiod by timePeriodSet x2 ) = fastPeriod kamaTimeperiod /4 kamaTimeperiod /2
change up is BUY, change down is SELL
 

* as 3 of the indicators return the same TREND (buy or sell) order executed by the TREND 
* on order close at profit of at least 1st trailing-step a new order executed to the opposide dircetion

-- 
Everything is dynamic by the: 

public double defFirstStep = 10;
public double minFirstStep = 2.5;

starting from the currentFoundBaseMinMaxToOneMin to work by

what happens EUR/USD price change waits for 15.0~Pip

while GBP/USD wait for difference of 70.0~Pip

the same regards the trailing-steps sizes.


currently parameters set:
* trade 32 instruments in-both directions
* max 4 order for each dircetions
* max Merges 3
* while new order of the same direction is executed as the previous has distance of 60% from the week's MAX-MIN
and merging as the 2nd order got 55% from the week's MAX-MIN

the " week's MAX-MIN" is at the configurations parameters the:

@Configurable("STDdev timePeriod")

public int STDminsPeriod = 168;

@Configurable("STDdev Period")

public Period STDPeriod = Period.ONE_HOUR;

