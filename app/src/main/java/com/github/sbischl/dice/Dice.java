package com.github.sbischl.dice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Dice {
    private SharedPreferences persistenDict;
    private int numberOfDices;
    private int numberOfSides;
    public static double[] probabilityArray;
    private Context context;
    private List<String> playerNames;
    private int numberOfPlayers;
    private boolean gameMode;
    private double probabilityCompensation = 0.5;

    public enum diceTypes {
        STANDARD,
        COMPENSATED
    }

    public Dice(SharedPreferences persistenDict, Context context) {
        this.context = context;
        this.persistenDict = persistenDict;
        if (!persistenDict.contains("init")) {
            //dictionary was not initilized. Do that now with default values
            this.numberOfDices = Integer.parseInt(context.getResources().getString(R.string.default_number_dices));
            persistenDict.edit().putInt("number_dices",numberOfDices).apply();

            this.numberOfSides = Integer.parseInt(context.getResources().getString(R.string.default_number_sides));
            persistenDict.edit().putInt("number_sides_dice",numberOfSides).apply();
            writeDictKeys();

            //last put bool init
            persistenDict.edit().putBoolean("init",true).apply();
        }
        else {
            //load the values
            this.numberOfSides = readNumberOfSides();
            this.numberOfDices = readNumberOfDices();
        }
        if (probabilityArray == null || probabilityArray.length != getNumberOfPossibleValues()) {
            probabilityArray = calculateProbabilityArray();
        }
        this.numberOfPlayers = persistenDict.getInt("number_of_players",0);
        this.gameMode = persistenDict.getBoolean("game_mode", false);
    }

    public int readNumberOfDices() {
        return persistenDict.getInt("number_dices",-1);
    }

    public void outputLog() {
        Log.d("DiceLog", "Outputting Counts:");
        int counter = 0;
        while(persistenDict.contains("side_count_" + counter)) {
            Log.d("DiceLog", "" + persistenDict.getInt("side_count_" + counter, -1));
            counter++;
        }
        Log.d("DiceLog", "Outputting ExpectedCounts:");
        counter = 0;
        while(persistenDict.contains("side_expected_count_" + counter)) {
            Log.d("DiceLog", "" + persistenDict.getFloat("side_expected_count_" + counter, -1));
            counter++;
        }
        counter = 0;
        Log.d("DiceLog", "Outputting Names:");
        while(persistenDict.contains("side_name_" + counter)) {
            Log.d("DiceLog", persistenDict.getString("side_name_" + counter, "not found"));
            counter++;
        }
        counter = 0;
        Log.d("DiceLog", "Outputting Last roll:");
        while(persistenDict.contains("last_roll_" + counter)) {
            Log.d("DiceLog","" + persistenDict.getInt("last_roll_" + counter, -1));
            counter++;
        }


    }

    public void purgeDict() {
        persistenDict.edit().clear().apply();
    }

    public int readNumberOfSides() {
        return persistenDict.getInt("number_sides_dice",-1);
    }

    public int getNumberOfDices() {
        return this.numberOfDices;
    }

    public int getNumberOfSides() {
        return this.numberOfSides;
    }

    public void setNumberOfDices(int numberOfDices) {
        this.numberOfDices = numberOfDices;
    }

    public void setNumberOfSides(int numberOfSides) {
        this.numberOfSides = numberOfSides;
    }

    public void writeNumberOfDices(int numberOfDices) {
        persistenDict.edit().putInt("number_dices", numberOfDices).apply();
    }

    public void writeNumberOfSides(int NumberOfSides) {
        persistenDict.edit().putInt("number_sides_dice", NumberOfSides).apply();
    }

    public void resetDictKeys() {
        clearDictKeys();
        writeDictKeys();
        if (gameMode) {
            removeGameDictKeys();
            writeGameDictKeys();
        }
    }

    public void setPlayerNames(List<String> playerNames) {
        this.playerNames = playerNames;
    }

    public void clearDictKeys() {
        int counter = 0;
        while(persistenDict.contains("side_count_" + counter)) {
            persistenDict.edit().remove("side_count_" + counter).apply();
            persistenDict.edit().remove("side_expected_count_" + counter).apply();
            counter++;
        }
        counter = 0;
        while(persistenDict.contains("side_name_" + counter)) {
            persistenDict.edit().remove("side_name_" + counter).apply();
            counter++;
        }
        counter = 0;
        while(persistenDict.contains("last_roll_" + counter)) {
            persistenDict.edit().remove("last_roll_" + counter).apply();
            counter++;
        }
    }



    public void writeDictKeys() {
        int counter = 0;
        int[] possibleValues = getPossibleValues();
        while(counter < possibleValues.length) {
            persistenDict.edit().putInt("side_count_" + counter, 0).apply();
            persistenDict.edit().putFloat("side_expected_count_" + counter, 0).apply();
            persistenDict.edit().putString("side_name_" + counter, "" + possibleValues[counter]).apply();
            counter++;
        }

        for (int i = 0; i < numberOfDices; i++) {
            int parseint = Integer.parseInt(context.getResources().getString(R.string.initial_number));
            persistenDict.edit().putInt("last_roll_" + i, parseint).apply();
        }
    }

    public void writeGameDictKeys() {
        for (int i = 0; i < numberOfPlayers; i++) {
            writePlayerKeys(i);
        }
    }

    public void writePlayerKeys(int player) {
        int counter = 0;
        int[] possibleValues = getPossibleValues();
        while(counter < possibleValues.length) {
            persistenDict.edit().putInt("player" + player  + "_side_count_" + counter, 0).apply();
            persistenDict.edit().putFloat("player" + player  + "_side_expected_count_" + counter, 0).apply();
            counter++;
        }
    }

    public void removeGameDictKeys() {
        for (int i = 0; i < numberOfPlayers; i++) {
            removePlayerKeys(i);
        }
    }

    public void removePlayerKeys(int player) {
        int counter = 0;
        while(persistenDict.contains("player" + player  + "_side_count_" + counter)) {
            persistenDict.edit().remove("player" + player + "_side_count_" + counter).apply();
            persistenDict.edit().remove("player" + player  + "_side_expected_count_" + counter).apply();
            counter++;
        }
    }

    public int[] getPossibleValues() {
        int[] returnArray = new int[getNumberOfPossibleValues()];
        int counter = numberOfDices;
        while(counter <= numberOfSides*numberOfDices) {
            returnArray[counter - numberOfDices] = counter;
            counter++;
        }
        return returnArray;
    }

    public int getNumberOfPossibleValues() {
        return numberOfSides*numberOfDices - numberOfDices + 1;
    }

    public void updateExpectedCount() {
        //get number of draws sofar:
        int draws = getNumberOfDraws();
        int counter = 0;
        while(persistenDict.contains("side_expected_count_" + counter)) {
            persistenDict.edit().putFloat("side_expected_count_" + counter, (float) (draws * probabilityArray[counter])).apply();
            counter++;
        }

        if (gameMode) {
                int currentPlayer = persistenDict.getInt("whose_turn", 0);
                int drawsPlayerI =  getNumberOfDrawsOfPlayer(currentPlayer);
                counter = 0;
                while(persistenDict.contains("player" + currentPlayer + "_side_expected_count_" + counter)) {
                    persistenDict.edit().putFloat("player" + currentPlayer + "_side_expected_count_" + counter, (float) (drawsPlayerI * probabilityArray[counter])).apply();
                    counter++;
            }
        }
    }

    public double[] calculateProbabilityArray() {
        double[] probabilites = new double[getNumberOfPossibleValues()];
        for (int i = numberOfDices; i <= numberOfDices* numberOfSides; i++) {
            probabilites[i-numberOfDices] = calculateSumProb(i);
        }
        return probabilites;
    }

    public double calculateSumProb(int sum) {
        int possibilites = 0;
        for (int i = 0; i <= (sum - numberOfDices) / numberOfSides ; i++) {
            possibilites += Math.pow(-1,i)*binomialcoef(numberOfDices, i) * binomialcoef(sum - 1 - numberOfSides*i, numberOfDices-1);
        }
        return possibilites * Math.pow(1.0/numberOfSides,numberOfDices);
    }

    public int getNumberOfDraws() {
        int counter = 0;
        int draws = 0;
        while(persistenDict.contains("side_count_" + counter)) {
            draws += persistenDict.getInt("side_count_" + counter, -1);
            counter++;
        }
        return draws;
    }

    public int getNumberOfDrawsOfPlayer(int player) {
        int counter = 0;
        int draws = 0;
        while(persistenDict.contains("player" + player + "_side_count_" + counter)) {
            draws += persistenDict.getInt("player" + player + "_side_count_" + counter, -1);
            counter++;
        }
        return draws;
    }



    public int binomialcoef(int n, int k) {
        if ((n == k) || (k == 0)) {
            return 1;
        }
        else {
            return binomialcoef(n - 1, k) + binomialcoef(n - 1, k - 1);
        }
    }

    public int[] rollStandardDice() {
        int[] returnArray = new int[numberOfDices];
        for (int i = 0; i < numberOfDices; i++) {
            returnArray[i] = rollSingleDice();
        }
        return returnArray;
    }

    public double sumOfDeviations() {
        // need to find the most underrepresented sum of spots so far.
        int counter = 0;
        int[] sideCounts = getCounts();


        int numberOfDraws = getNumberOfDraws();

        // Multiply with number of draws to get expected value THEN compare with actual count
        // Remember the one with the highest deviation
        double sumOfDeviations = 0;
        for (int i = 0; i < probabilityArray.length; i++) {
            double expectedValue = probabilityArray[i] * numberOfDraws;
            double actualCount = sideCounts[i];
            sumOfDeviations += Math.abs(expectedValue - actualCount);
        }
        Log.d("DiceLog", "Sum of Deviations from expected Value:" + sumOfDeviations);
        return sumOfDeviations;
    }

    public int[] rollCompensatedDice() {
        Random random = new Random();
        int numberOfDraws = getNumberOfDraws();
        double randomDouble = random.nextDouble();
        if (randomDouble < probabilityCompensation && numberOfDraws >= 10) {
            // make a compenstation by rolling the most underrepresented sum of spots
            int[] returnArray = new int[numberOfDices];

            // need to find the most underrepresented sum of spots so far.
            int counter = 0;
            // Get counts of each sum of spots so far
            int[] sideCounts = getCounts();

            // Multiply with number of draws to get expected value THEN compare with actual count
            // Remember the one with the highest deviation
            double highestdeviation = 0;
            ArrayList<Integer> indexOfHighestDeviation = new ArrayList<>();
            for (int i = 0; i < probabilityArray.length; i++) {
                double expectedValue = probabilityArray[i] * (numberOfDraws+1);
                double actualCount = sideCounts[i];
                double deviation = expectedValue - actualCount;
                if (deviation > highestdeviation) {
                    highestdeviation = deviation;
                    indexOfHighestDeviation.clear();
                    indexOfHighestDeviation.add(i);
                }
                else if (deviation == highestdeviation) {
                    indexOfHighestDeviation.add(i);
                }
            }
            // This is the actual roll. If there are multiple sums of spots that have the same
            // deviation choose randomly
            int indexOfRollInList = (int) (indexOfHighestDeviation.size() * random.nextDouble());
            int indexOfRoll = indexOfHighestDeviation.get(indexOfRollInList);
            int sumOfSpots =  getPossibleValues()[indexOfRoll];

            // Now that we know the sum of spots, we need to figure out the individual dices and store
            // the roll of each dice in the returnArray
            int remainder = sumOfSpots;
            for (int i = 0; i < returnArray.length; i++) {
                int IthDraw = remainder / (returnArray.length - i);
                returnArray[i] = IthDraw;
                remainder = remainder - IthDraw;
            }
            // Log this to see when we throw a compensation
            // Log.d("DiceLog", "Rolled a compensation:" + Arrays.toString(returnArray));
            return returnArray;
        }
        else {
            // Log.d("DiceLog", "Standard roll");
            return rollStandardDice();
        }
    }

    public int[] getCounts() {
        int[] sideCounts = new int[getNumberOfPossibleValues()];
        int counter = 0;
        while(persistenDict.contains("side_count_" + counter)) {
            sideCounts[counter] = persistenDict.getInt("side_count_" + counter, -1);
            counter++;
        }
        return sideCounts;
    }

    public int[] getLastRoll() {
        int[] lastRolls = new int[numberOfDices];
        for (int i = 0; i < numberOfDices; i++) {
            lastRolls[i] = persistenDict.getInt("last_roll_" + i, -1);
        }
        return lastRolls;
    }

    public int rollSingleDice() {
        // Returns with equal probability 1 to number of sides

        Random random = new Random();
        double randomDouble = random.nextDouble();

        // This is most likely unnecessary but for the super unlikely case that the random double is
        // exactly one, i have to redraw such that I cant roll a 7 on a dice with 6 sides.
        while (randomDouble == 1) {
            randomDouble = random.nextDouble();
        }
        return(int)(1 + randomDouble*numberOfSides);
    }

    public int[] rollAndStore(diceTypes diceType) {
        int[] rolls;
        switch(diceType) {
            case STANDARD:
                rolls = rollStandardDice();
                break;
            case COMPENSATED:
                rolls = rollCompensatedDice();
                break;
            default:
                //throw new IllegalStateException("Unexpected value: " + diceType);
                // default to standard dice
                rolls = rollStandardDice();
                break;
        }

        int sum = 0;
        for (int i = 0; i < rolls.length; i++) {
            persistenDict.edit().putInt("last_roll_" + i, rolls[i]).apply();
            sum += rolls[i];
        }
        // Get the current count for the sum:
        int count = persistenDict.getInt("side_count_" + (sum - numberOfDices), -1);
        persistenDict.edit().putInt("side_count_" + (sum -numberOfDices), count + 1).apply();

        if (gameMode) {
            // Store rolls and expected value for each player
            int player = persistenDict.getInt("whose_turn", 0);
            count = persistenDict.getInt("player" + player +
                    "_side_count_" + (sum - numberOfDices), -1);
            persistenDict.edit().putInt("player" + player + "_side_count_" + (sum -numberOfDices), count + 1).apply();
        }

        updateExpectedCount();

        return rolls;
    }

    public String whoseTurn() {
        int playerIndex = persistenDict.getInt("whose_turn", 0);
        // Need to get the name of this player
        return persistenDict.getString("playername_" + playerIndex, "Null");
    }

    public boolean gameMode() {
        return gameMode;
    }

    public void simulate(int repeats) {
        for (int i = 0; i < repeats; i++) {
            rollAndStore(diceTypes.COMPENSATED);
        }
    }

    public void updateNext() {
        int currentPlayer = persistenDict.getInt("whose_turn", 0);
        if (currentPlayer == numberOfPlayers - 1) {
            persistenDict.edit().putInt("whose_turn", 0).apply();
        }
        else {
            persistenDict.edit().putInt("whose_turn", currentPlayer + 1).apply();
        }
    }
}
