package data;


public class messageTuple{
    
    private int clientId;
    private int replicaId;
    private int requestNum;
    public String messageDirection;
    private String newStateValue;

    public messageTuple(int clientId, int replicaId, int requestNum,String messageDirection, String newStateValue){
        this.clientId = clientId;
        this.replicaId = replicaId;
        this.requestNum = requestNum;
        this.messageDirection = messageDirection;
        this.newStateValue = newStateValue;
    }

    public int getRequestNum(){
        return this.requestNum; 
    }

    public String getNewStateValue(){
        return this.newStateValue;
    }

    public void setMessageDirection(String newMessageDirection){
        this.messageDirection = newMessageDirection;
    }

    public String toString(){
        return "<" + clientId + ", "+ replicaId + ", " + requestNum + ", " + messageDirection + ", " + newStateValue +">";
    }

    public String toPrintString(){
        return "<" + clientId + ", "+ replicaId + ", " + requestNum + ", " + messageDirection + ">";
    }


    public static messageTuple fromString(String tupleString){
        String[] arr = tupleString.substring(1, tupleString.length()-1).split(", ");

        int clientId = Integer.parseInt(arr[0]);
        int replicaId =  Integer.parseInt(arr[1]);
        int requestNum =  Integer.parseInt(arr[2]);
        String messageDirection = (arr[3]);
        String newStateValue = (arr[4]);

        return new messageTuple(clientId, replicaId, requestNum, messageDirection, newStateValue);
        
    }

}
