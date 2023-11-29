package data;


public class checkPointMessageTuple{
    
    private String newStateValue;
    private int checkPointCount;
    public int servernum; 

    public checkPointMessageTuple(String newStateValue, int checkPointCount, int servernum){
        this.newStateValue = newStateValue;
        this.checkPointCount =checkPointCount;
        this.servernum = servernum;
    }

    public int getCheckpointCount(){
        return this.checkPointCount; 
    }

    public String getNewStateValue(){
        return this.newStateValue;
    }

    public int getServerNum(){
        return this.servernum;
    }

    public String toString(){
        return "<checkpoint, " + newStateValue + ", "+ checkPointCount + ", " + servernum + ">";
    }


    public static checkPointMessageTuple fromString(String tupleString){
        String[] arr = tupleString.substring(1, tupleString.length()-1).split(", ");
        String newStateValue = (arr[1]);
        int checkPointCount =  Integer.parseInt(arr[2]);
        int servernum = Integer.parseInt(arr[3]);
        return new checkPointMessageTuple(newStateValue, checkPointCount, servernum);
    }

}
