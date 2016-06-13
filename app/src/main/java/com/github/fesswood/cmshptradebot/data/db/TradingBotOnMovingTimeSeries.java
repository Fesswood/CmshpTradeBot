package com.github.fesswood.cmshptradebot.data.db;

/**
 * Created by fesswood on 31.05.16.
 */

import android.util.Log;

import com.github.fesswood.cmshptradebot.app.App;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.DbRepository;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticModel;
import com.github.fesswood.cmshptradebot.data.db.TradeStatistic.TradeStatisticObjectMapper;
import com.github.fesswood.cmshptradebot.data.event.TradeEvent;

import eu.verdelhan.ta4j.AnalysisCriterion;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.CashFlow;
import eu.verdelhan.ta4j.analysis.criteria.AverageProfitableTradesCriterion;
import eu.verdelhan.ta4j.analysis.criteria.RewardRiskRatioCriterion;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import eu.verdelhan.ta4j.analysis.criteria.VersusBuyAndHoldCriterion;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p>
 */
public class TradingBotOnMovingTimeSeries {

    private static final String TAG = TradingBotOnMovingTimeSeries.class.getSimpleName();
    /**
     * Close price of the last tick
     */
    private static Decimal LAST_TICK_CLOSE_PRICE;
    private TimeSeries series;
    private Strategy strategy;
    private TradingRecord tradingRecord;
    private StragegyListener mListener;
    private long mInitTimeStamp;
    private CashFlow cashFlow;
    private int buyCount = 0;
    private Decimal mBuyPrice = null;

    /**
     * Builds a moving time series (i.e. keeping only the maxTickCount last ticks)
     *
     * @param maxTickCount the number of ticks to keep in the time series (at maximum)
     * @return a moving time series
     */
    public static TimeSeries initMovingTimeSeries(int maxTickCount) {
        List<TradeStatisticModel> data = DbRepository.getAllTradeStatisticModel();
        List<Tick> ticks = new ArrayList<>();
        for (TradeStatisticModel model : data) {
            ticks.add(TradeStatisticObjectMapper.mapToTick(model));
        }
        TimeSeries series = new TimeSeries(ticks);
        Log.w(TAG, "Initial tick count: " + series.getTickCount());
        // Limitating the number of ticks to maxTickCount
        series.setMaximumTickCount(maxTickCount);
        LAST_TICK_CLOSE_PRICE = series.getTick(series.getEnd()).getClosePrice();
        Log.w(TAG, " (limited to " + maxTickCount + "), close price = " + LAST_TICK_CLOSE_PRICE);
        return series;
    }

    /**
     * @param series a time series
     * @return a dummy strategy
     */
    private static Strategy buildStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        SMAIndicator longSma = new SMAIndicator(closePrice, 30);
        Rule buyingRule = new OverIndicatorRule(shortSma, longSma)
                .or(new CrossedDownIndicatorRule(closePrice, Decimal.valueOf("0.235")));

        Rule sellingRule = new UnderIndicatorRule(shortSma, longSma)
                .or(new CrossedDownIndicatorRule(closePrice, Decimal.valueOf("0.290")));

        // Signals
        // Buy when SMA goes over close price
        // Sell when close price goes over SMA
        Strategy buySellSignals = new Strategy(
                buyingRule, sellingRule
        );
        return buySellSignals;
    }

    /**
     * Generates a random decimal number between min and max.
     *
     * @param min the minimum bound
     * @param max the maximum bound
     * @return a random decimal number between min and max
     */
    private static Decimal randDecimal(Decimal min, Decimal max) {
        Decimal randomDecimal = null;
        if (min != null && max != null && min.isLessThan(max)) {
            randomDecimal = max.minus(min).multipliedBy(Decimal.valueOf(Math.random())).plus(min);
        }
        return randomDecimal;
    }

    /**
     * Generates a random tick.
     *
     * @return a random tick
     */
    private static Tick generateRandomTick() {
        final Decimal maxRange = Decimal.valueOf("0.03"); // 3.0%
        Decimal openPrice = LAST_TICK_CLOSE_PRICE;
        Decimal minPrice = openPrice.minus(openPrice.multipliedBy(maxRange.multipliedBy(Decimal.valueOf(Math.random()))));
        Decimal maxPrice = openPrice.plus(openPrice.multipliedBy(maxRange.multipliedBy(Decimal.valueOf(Math.random()))));
        Decimal closePrice = randDecimal(minPrice, maxPrice);
        LAST_TICK_CLOSE_PRICE = closePrice;
        return new Tick(DateTime.now(), openPrice, maxPrice, minPrice, closePrice, Decimal.ONE);
    }

    public void init(StragegyListener listener) {
        Log.w(TAG, "********************** Initialization **********************");
        mListener = listener;
        // Getting the time series
        series = initMovingTimeSeries(500);
        // Time stamp when the first Tick has been received by bot
        mInitTimeStamp = series.getLastTick().getEndTime().toDate().getTime();
        // Building the trading strategy
        strategy = buildStrategy(series);
        // Initializing the trading history
        tradingRecord = new TradingRecord();
        // Getting the cash flow of the resulting trades
        cashFlow = new CashFlow(series, tradingRecord);

    }

    public void addTicksFromDb() {
        List<TradeStatisticModel> statisticModels = DbRepository.getAllTradeStatisticModelAfter(mInitTimeStamp);
        if (!statisticModels.isEmpty()) {
            mInitTimeStamp = statisticModels.get(statisticModels.size() - 1).getTimestamp();
            for (TradeStatisticModel statisticItem : statisticModels) {
                series.addTick(TradeStatisticObjectMapper.mapToTick(statisticItem));
            }

        }
    }

    public void checkStrategy() {
        int endIndex = series.getEnd();
        Tick newTick = series.getLastTick();
        Decimal closePrice = newTick.getClosePrice();
        if (strategy.shouldEnter(endIndex)
                && buyCount < 20) {
            // Our strategy should enter
            Log.w(TAG, "Strategy should ENTER on " + endIndex);
            mBuyPrice = closePrice;
            boolean entered = tradingRecord.enter(endIndex, closePrice, Decimal.TEN);
            mListener.enter(closePrice.toDouble(), 10);
            buyCount += 10;
            if (entered) {
                Order entry = tradingRecord.getLastEntry();
                Log.i(TAG, "Entered on " + entry.getIndex()
                        + " (price=" + entry.getPrice().toDouble()
                        + ", amount=" + entry.getAmount().toDouble() + ")");
            }
        } else if (strategy.shouldExit(endIndex)
                && buyCount > 0
                && mBuyPrice != null
                && mBuyPrice.isLessThan(closePrice)) {
            // Our strategy should exit
            //  strategy.sho
            buyCount -= 10;
            Log.w(TAG, "Strategy should EXIT on " + endIndex);
            mListener.exit(closePrice.toDouble(), 10);
            boolean exited = tradingRecord.exit(endIndex, closePrice, Decimal.TEN);
            if (exited) {
                Order exit = tradingRecord.getLastExit();
                Log.i(TAG, "Exited on " + exit.getIndex()
                        + " (price=" + exit.getPrice().toDouble()
                        + ", amount=" + exit.getAmount().toDouble() + ")");
            }
        } else {
            String message = "Strategy should HOLD on index " + endIndex;
            Log.w(TAG, message);
            App.getBus().post(new TradeEvent("\n " + new Date() + " " + message));
        }
    }

    public String calculateProfit() {
        // Getting the profitable trades ratio
        AnalysisCriterion profitTradesRatio = new AverageProfitableTradesCriterion();
        String result = "\n Profitable trades ratio: " + profitTradesRatio.calculate(series, tradingRecord);
        // Getting the reward-risk ratio
        AnalysisCriterion rewardRiskRatio = new RewardRiskRatioCriterion();
        result += ("\n Reward-risk ratio: " + rewardRiskRatio.calculate(series, tradingRecord));
        // Total profit of our strategy
        // vs total profit of a buy-and-hold strategy
        AnalysisCriterion vsBuyAndHold = new VersusBuyAndHoldCriterion(new TotalProfitCriterion());
        result += "\n Our profit vs buy-and-hold profit: " + vsBuyAndHold.calculate(series, tradingRecord);
        return result;
    }

    public interface StragegyListener {
        public void exit(double price, int amount);
        public void enter(double price, int amount);
    }
}