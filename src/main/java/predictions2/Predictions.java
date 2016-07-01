package predictions2;

import javax.servlet.ServletContext;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by AlexY on 2016/6/28.
 */
public class Predictions {


    private ConcurrentMap<Integer, Prediction> predictions;

    private ServletContext servletContext;

    private AtomicInteger mapKey;

    public Predictions() {

        predictions = new ConcurrentHashMap<>();
        mapKey = new AtomicInteger();

    }

    //    Servlet被用来从war文件中的text文件中读取数据
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext sctx) {
        this.servletContext = sctx;
    }

    //空实现
    public void setMap(ConcurrentMap<String, Prediction> predicitons) {

    }


    //    getPredictions返回一个表示Predictions 数组的xml文件
    public ConcurrentMap<Integer, Prediction> getMap() {

//        检测是否设置了ServletContext
        if (getServletContext() == null) {

            return null;
        }

//        检测是否已经读取了数据
        if (predictions.size() < 1) {

            populate();
        }


        return predictions;


    }

    //      将Predictions数组转换为xml文档
    public String toXML(Object obj) {

        String xml = null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(out);

        encoder.writeObject(obj);  //序列化为xml
        encoder.close();

        xml = out.toString();


        return xml;

    }


    public int addPrediction(Prediction p) {
        int id = mapKey.incrementAndGet();

        p.setId(id);
        predictions.put(id, p);

        return id;

    }


    //    将文本中的内容读取到predictions数组
    private void populate() {

        String filename = "/WEB-INF/data/predictions.db";

        InputStream in = servletContext.getResourceAsStream(filename);


//        将文本文件中的内容读取到predictions数组
        if (null != in) {

            try {

//            因为文件中每条记录就是一行，而每一行who和what字段是通过"!"分隔的
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(isr);

                String line = null;

                int i = 0;

                while (null != (line = reader.readLine())) {

                    String[] parts = line.split("!");
                    Prediction p = new Prediction();

                    p.setWho(parts[0]);
                    p.setWhat(parts[1]);


                    addPrediction(p);


                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


    }


}
