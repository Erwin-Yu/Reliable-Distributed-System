package data;


public class checkPointMessageTuple{
    
    private String newStateValue;
    private int checkPointCount;

    public checkPointMessageTuple(String newStateValue, int checkPointCount){
        this.newStateValue = newStateValue;
        this.checkPointCount =checkPointCount;
    }

    public int getCheckpointCount(){
        return this.checkPointCount; 
    }

    public String getNewStateValue(){
        return this.newStateValue;
    }

    public String toString(){
        return "<checkpoint, " + newStateValue + ", "+ checkPointCount + ">";
    }

    // public String toPrintString(){
    //     return "<" + newStateValue + ", "+ checkPointCount + ">";
    // }

    public static checkPointMessageTuple fromString(String tupleString){
        String[] arr = tupleString.substring(1, tupleString.length()-1).split(", ");
        String newStateValue = (arr[1]);
        int checkPointCount =  Integer.parseInt(arr[2]);
        return new checkPointMessageTuple(newStateValue, checkPointCount);
    }

}
