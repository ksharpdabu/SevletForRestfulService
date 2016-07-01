package predictions2;

import java.io.Serializable;

/**
 * Created by AlexY on 2016/6/28.
 */
public class Prediction implements Serializable ,Comparable<Prediction>{


    private static final long serialVersionUID = 7570089771880396982L;



    private String who;  // 人
    private String what; //他的prediction
    private int id; //作为搜索的key

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Prediction() {
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    @Override
    public int compareTo(Prediction o) {

        return this.id - o.id;
    }
}
