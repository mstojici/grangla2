package com.boardgame.miljac.grangla.gameplay;

import java.util.Random;


/**
 * This is where the actual game state is stored
 */

public class Table
{
    private final Space[][] table = new Space[10][10];

    private final int[] no=           {0,0,0,1,0,0,0};
    private final int[] win6=         {1,1,1,1,1,1};
    private final int[] win5=         {1,1,1,1,1,1};
    private final int[] win=          {1,1,1,1};

    private final int[] probablyMust= {0,1,1,1,0};

    private final int[] three1=       {0,1,1,1};
    private final int[] three2=       {1,0,1,1};
    private final int[] three3=       {1,1,0,1};
    private final int[] three4=       {1,1,1,0};

    private final int[] two1=          {0,1,1,0,0};
    private final int[] two2=          {0,0,1,1,0};
    private final int[] two3=          {0,1,0,1,0};

    private final int[] shittierTwo1= {0,1,0,1};
    private final int[] shittierTwo2= {1,0,1,0};
    private final int[] shittierTwo3= {1,1,0,0};
    private final int[] shittierTwo4= {0,1,1,0};
    private final int[] shittierTwo5= {0,0,1,1};
    private final int[] shittierTwo6= {1,0,0,1};

    private double level;
    private double mistakeFactor;
    private long lastEventTime = 0;
    private Coordinates lastMove = new Coordinates(5,5);
    private long lastRockMove = 0;

    private Random random = new Random();
    boolean server = false;


    /**
     * The default constructor. Initializes an empty board.
     */

    public Table(int l)
    {
        this.level = l;
        this.mistakeFactor = (1.2 * (level/10 - 10)*(level/10 - 10)*(level/10 - 10)*(level/10 - 10));

        for (int i=0; i<TableConfig.TABLE_SIZE; i++)
        {
            for (int j=0; j<TableConfig.TABLE_SIZE; j++){
                (this.table[i][j]) = new Space();
            }
        }

        Boolean rockPut;
        for (int i=0; i<(TableConfig.NO_OF_ROCKS); i++){
            rockPut = false;
            while(!rockPut) {
                int x = (int) (random.nextDouble() * TableConfig.TABLE_SIZE);
                int y = (int) (random.nextDouble() * TableConfig.TABLE_SIZE);
                if (get(x, y) == State.empty) {
                    put(State.rock, x, y);
                    rockPut = true;
                }
            }

        }
    }

    /**
     * Puts a mark on the board.
     * @param givenState a mark to be put.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     */

    public void put(State givenState, int i, int j)
    {
        i = (i + TableConfig.TABLE_SIZE*2) % TableConfig.TABLE_SIZE;
        j = (j + TableConfig.TABLE_SIZE*2) % TableConfig.TABLE_SIZE;
        (this.table[i][j]).setState(givenState);
    }

    public synchronized boolean putIfPossible(State givenState, int i, int j)
    {
        if(this.get(i,j) == State.empty) {
            this.put(givenState, i, j);
            lastMove = new Coordinates(i, j);
            return true;
        }
        return false;
    }


    public synchronized boolean emptyIfPossible(int i, int j)
    {
        if(this.get(i,j) != State.empty) {
                this.put(State.empty, i, j);
                lastMove = new Coordinates(i, j);
            return true;
        }
        return false;
    }

    /**
     * Finds out which mark is on the specific field on the board.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     * @return a mark on the field with given coordinates
     */
    public State get(int i, int j)
    {
        try {
            return (this.table[normalizeIndex(i)][normalizeIndex(j)]).getState();
        }
        catch (Exception e) {
            return null;
        }
    }

    synchronized State getAndMoveRock(int i, int j)
    {
        if (((int) (random.nextDouble() * TableConfig.ROCK_MOVEMENT_PROBABILITY) == 1) &&
                !((System.currentTimeMillis() - lastEventTime) < 200) &&
                !((System.currentTimeMillis() - lastRockMove) < 2000)) {
            moveRock();
            lastRockMove = System.currentTimeMillis();
        }
        return this.get(i, j);
    }



    /**
     * Used when a computer is playing to make a move.
     * @param me
     */
    public synchronized Coordinates calculateAndMakeAMove(State me)
    {
        Double weight;
        Double randomFactor;
        State enemy;
        double biggestWeight=-1;
        int bWICoor=1,bWJCoor=1;

        if (me==State.cross)
            enemy= State.circle;
        else
            enemy= State.cross;

        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = (lastMove.y-3); j < (lastMove.y+4); j++) {
                randomFactor = random.nextDouble();
                randomFactor = randomFactor*randomFactor*randomFactor * mistakeFactor;

                weight = this.evaluateSpaceWeight(i, j, me) + randomFactor;

                if (weight > biggestWeight) {
                    bWICoor = i;
                    bWJCoor = j;
                    biggestWeight = weight;
                }
            }
        }

        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = (lastMove.y-3); j < (lastMove.y+4); j++) {
                weight = 0.8 * this.evaluateSpaceWeight(i, j, enemy);
                if (weight > biggestWeight) {
                    bWICoor = i;
                    bWJCoor = j;
                    biggestWeight = weight;
                }
            }
        }
        this.put(me,bWICoor,bWJCoor);
        lastMove = new Coordinates(bWICoor,bWJCoor);

        return (new Coordinates(bWICoor, bWJCoor));

    }


    /**
     * Detects how many instances of a specific sequence
     * on a board could be made by putting a mark on the
     * field with given coordinates.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     * @param me a mark being put on the board and tested
     * @param sequence an array containing the sequence, 1 represents a mark, and 0 represents an empty space
     * @return
     */

    private int detectSequence(int i, int j, State me, int[] sequence, int direction)
    {
        if (this.get( i, j )!=State.empty) return 0;
        int result=0;
        this.put( me, i, j );

        for (int begin=0; begin<sequence.length; begin++)
        {
            int count[]={0,0,0,0};
            for (int x=0; x<sequence.length; x++)
            {
                switch(direction) {
                    case 0:
                        if (((this.get(i - (sequence.length - 1) + begin + x, j) == me) && (sequence[x] == 1)) ||
                                (this.get(i - (sequence.length - 1) + begin + x, j) == State.empty) && (sequence[x] == 0))    //vodoravno
                            count[0]++;
                        break;
                    case 1:
                        if (((this.get(i, j - (sequence.length - 1) + begin + x) == me) && (sequence[x] == 1)) ||
                                (this.get(i, j - (sequence.length - 1) + begin + x) == State.empty) && (sequence[x] == 0)) //okomito
                            count[1]++;
                        break;
                    case 2:
                        if (((this.get(i - (sequence.length - 1) + begin + x, j - (sequence.length - 1) + begin + x) == me) && (sequence[x] == 1)) ||
                                (this.get(i - (sequence.length - 1) + begin + x, j - (sequence.length - 1) + begin + x) == State.empty) && (sequence[x] == 0))
                            count[2]++;
                        break;
                    case 3:
                        if (((this.get(i - (sequence.length - 1) + begin + x, j + (sequence.length - 1) - begin - x) == me) && (sequence[x] == 1)) ||
                                (this.get(i - (sequence.length - 1) + begin + x, j + (sequence.length - 1) - begin - x) == State.empty) && (sequence[x] == 0))
                            count[3]++;
                        break;
                }
            }

            for (int x=0; x<4; x++)
                if (count[x]==sequence.length)
                    result++;

        }
        this.put( State.empty, i, j );
        return result;
    }


    /**
     * Evaluates a weight of a single field. Bigger weight means it's probably better to make a move on that field.
     *
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     * @param me a player about who'soloNote move it is been thought.
     * @return Weight
     */

    private double evaluateSpaceWeight(int i, int j, State me)
    {
        double result=0;
        if (this.get( i, j )!=State.empty) return -100000;

        for (int x = 0; x<4; x++) {
            if ((this.detectSequence(i, j, me, no, x)) != 0) ;
            else if ((this.detectSequence(i, j, me, win6, x)) != 0)
                result += 60000;
            else if ((this.detectSequence(i, j, me, win5, x)) != 0)
                result += 30000;
            else if ((this.detectSequence(i, j, me, win, x)) != 0)
                result += 10000;
            else if ((this.detectSequence(i, j, me, probablyMust, x)) != 0)
                result += 1000;
            else if ((this.detectSequence(i, j, me, three1, x) != 0) ||
                    (this.detectSequence(i, j, me, three2, x) != 0) ||
                    (this.detectSequence(i, j, me, three3, x) != 0) ||
                    (this.detectSequence(i, j, me, three4, x) != 0))
                result += 100;
            else if ((this.detectSequence(i, j, me, two1, x) != 0) ||
                    (this.detectSequence(i, j, me, two2, x) != 0) ||
                    (this.detectSequence(i, j, me, two3, x) != 0))
                result += 10;
            else if ((this.detectSequence(i, j, me, shittierTwo1, x) != 0) ||
                    (this.detectSequence(i, j, me, shittierTwo2, x) != 0) ||
                    (this.detectSequence(i, j, me, shittierTwo3, x) != 0) ||
                    (this.detectSequence(i, j, me, shittierTwo4, x) != 0) ||
                    (this.detectSequence(i, j, me, shittierTwo5, x) != 0) ||
                    (this.detectSequence(i, j, me, shittierTwo6, x) != 0))
                result += 1;
        }

        if(normalizeIndex(i) == 0 ||
                normalizeIndex(i) == TableConfig.TABLE_SIZE - 1 ||
                normalizeIndex(j) == 0 ||
                normalizeIndex(j) == TableConfig.TABLE_SIZE - 1){
            result *= 0.9;
        }

        return result;
    }



    /**
     * Checks if someone has collected a sequence, and removes it from the table
     * @return A number of points
     */

    public synchronized int getScore(int iC, int jC, long lastEventT, int[][] sequenceDirections){
        int result = 0;
        lastEventTime = lastEventT;
        State state = this.get(iC, jC);
        Boolean found = false;

        for (int direction = 0; direction < 4; direction++) {
            int horizontal, vertical;
            switch (direction) {
                case 0:
                    horizontal = 1; vertical = 0;
                    break;
                case 1:
                    horizontal = 0; vertical = 1;
                    break;
                case 2:
                    horizontal = 1; vertical = 1;
                    break;
                default:
                    horizontal = 1; vertical = -1;
                    break;


            }

            int verticalStep = vertical;
            if (vertical == 0) verticalStep = 1;

            for (int i = iC - 3 * horizontal; i != iC + 1; i = i + 1) {
                for (int j = jC - 3 * vertical; j != jC + verticalStep; j = j + verticalStep) {

                    if ((this.get(i, j).equals(state)) &&
                            (this.get(i + 1 * horizontal, j + 1 * vertical).equals(state)) &&
                            (this.get(i + 2 * horizontal, j + 2 * vertical).equals(state)) &&
                            (this.get(i + 3 * horizontal, j + 3 * vertical).equals(state))) {

                        this.put(State.empty, i, j);
                        sortOutDirections(sequenceDirections, i, j, direction);
                        this.put(State.empty, i + horizontal, j + vertical);
                        sortOutDirections(sequenceDirections, i + horizontal, j + vertical, direction);
                        this.put(State.empty, i + 2 * horizontal, j + 2 * vertical);
                        sortOutDirections(sequenceDirections, i + 2 * horizontal, j + 2 * vertical, direction);
                        this.put(State.empty, i + 3 * horizontal, j + 3 * vertical);
                        sortOutDirections(sequenceDirections, i + 3 * horizontal, j + 3 * vertical, direction);
                        if(result == 0)
                            result += 9;
                        else
                            result += 10;

                        if (this.get(i + 4 * horizontal, j + 4 * vertical).equals(state)) {
                            this.put(State.empty, i + 4 * horizontal, j + 4 * vertical);
                            sortOutDirections(sequenceDirections, i + 4 * horizontal, j + 4 * vertical, direction);
                            result += 4;
                            if (this.get(normalizeIndex(i + 5 * horizontal), normalizeIndex(j + 5 * vertical)).equals(state)) {
                                this.put(State.empty, normalizeIndex(i + 5 * horizontal), normalizeIndex(j + 5 * vertical));
                                sortOutDirections(sequenceDirections, i + 5 * horizontal, j + 5 * vertical, direction);
                                result += 4;
                                if (this.get(i + 6 * horizontal, j + 6 * vertical).equals(state)) {
                                    this.put(State.empty, i + 6 * horizontal, j + 6 * vertical);
                                    sortOutDirections(sequenceDirections, i + 6 * horizontal, j + 6 * vertical, direction);
                                    result += 4;
                                }
                            }
                        }
                        found = true;
                        this.put(state, iC, jC);

                    }
                }
            }
        }

        if (found == true) {
            this.put(State.empty, iC, jC);
        }
        return result;

    }

    void sortOutDirections(int[][] spaceHeading, int i, int j, int counter){
        if (spaceHeading[normalizeIndex(i)][normalizeIndex(j)] != 0 &&
                spaceHeading[normalizeIndex(i)][normalizeIndex(j)] != counter + 1) {

            spaceHeading[normalizeIndex(i)][normalizeIndex(j)] = -1;
        } else {
            spaceHeading[normalizeIndex(i)][normalizeIndex(j)] = counter + 1;
        }
    }

    void moveRock(){
        if(!server) return;
        int rockToMoveNo = (int) (Math.ceil(random.nextDouble() * TableConfig.NO_OF_ROCKS) -1);
        int rockCount = 0;
        for (int i=0; i<TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                if (this.get(i, j) == State.rock){
                    if(rockCount == rockToMoveNo){
                        this.put(State.empty, i, j);
                    }
                    rockCount++;
                }
            }
        }

        Boolean rockPut = false;
        while(!rockPut) {
            int x = (int) (random.nextDouble() * TableConfig.TABLE_SIZE);
            int y = (int) (random.nextDouble() * TableConfig.TABLE_SIZE);
            if (get(x, y) == State.empty) {
                put(State.rock, x, y);
                rockPut = true;
            }
        }
    }

    public static int normalizeIndex(int i){
        return (i + TableConfig.TABLE_SIZE*2) % TableConfig.TABLE_SIZE;
    }
}
