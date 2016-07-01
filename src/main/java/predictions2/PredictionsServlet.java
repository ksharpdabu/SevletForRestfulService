package predictions2;

import org.json.JSONObject;
import org.json.XML;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;


/**
 * Created by AlexY on 2016/6/29.
 */
public class PredictionsServlet extends HttpServlet {


    private Predictions predictions; // 后端的bean

    //init方法会在servlet第一次加载到容器的时候被执行
//    创建一个Predictions对象，并设置它的servletContext属性
//    这样就可以做I/O操作了
    @Override
    public void init() throws ServletException {
       predictions = new Predictions();

        predictions.setServletContext(this.getServletContext());

    }


//    get方法用于读取
    // GET /predictions2
    // GET /predictions2?id=1
    // If the HTTP Accept header is set to application/json (or an equivalent
    // such as text/x-json), the response is JSON and XML otherwise.
    //如果http Accept header被设置为“application/json”（或者是等价的“text/x-json”），
//    则响应的是json，否则是xml
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String param = req.getParameter("id");
        Integer key = (param == null) ? null: new Integer(param);


//         通过检测HTTP header中 Accept字段 来获取用户是想要json还是xml
        boolean json = false;
        String accept  = req.getHeader("accept");
        if ( null != accept && accept.contains("json")){

            json = true;
            System.out.println("accept:"+accept);

        }

//        如果没有查询的字符串，则默认为客户端想要完整的list（所有的prediciton）
        if ( null == key){
            ConcurrentMap<Integer, Prediction> map = predictions.getMap();

//            对输出的列表进行排序
            Object[] list = map.values().toArray();
            Arrays.sort(list);

            String xml = predictions.toXML(list);
            System.out.println("key = null");
            sendResponse(resp,xml,json);

        }else {

            Prediction pred = predictions.getMap().get(key);

            if (null == pred){
                String msg = key + "doesn't not map to a prediciton.\n";
                sendResponse(resp,predictions.toXML(msg),false);
            }else {
                sendResponse(resp,predictions.toXML(pred),json);
            }
        }


    }

//    post方法用于创建,注意：必须是x-www-form-urlencoded
    // POST /predictions2
    // HTTP body 包含两个参数, one for the predictor ("who") and
    // another for the prediction ("what").

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String who = req.getParameter("who");
        String what= req.getParameter("what");

//        两个参数都不能为null
        if ( null == what || null == who){

            throw  new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
        }

//        创建一个 Precdiction
        Prediction p = new Prediction();
        p.setWho(who);
        p.setWhat(what);

//        为新的Prediction对象设置id属性，并报错
        int id= predictions.addPrediction(p);

//        生成确认信息
        String msg = "Prediction: {" + id + "} created.\n";
        sendResponse(resp,msg,false);



    }


//    put请求用于更新记录,
// postman中使用raw来发送参数如：id=33#who=Homer Allision
    // 使用culr  : url --request PUT --data "id=35#what=This is an update" localhost:8080/predictions2
    // PUT /predictions
    // HTTP body 应该至少包含两个参数, 一个是prediciton的id（用于查找），
// 另一个可以是prediction的who或者what（用于更新）
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String key = null;
        String rest = null;
        boolean who = false;  //用于判断到底是更新who还是what

        BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String data = br.readLine();

//        假设put请求有两个参数：id和 who（或what）。再假设id在前面。
//        在客户端会有一个“#”作为两个参数的分隔符号，如下：
//        id=33#who=Homer Allision
        String[] args = data.split("#"); // id是args[0] rest是args[1]
        String[] parts1 = args[0].split("=");
        key = parts1[1];
        System.out.println("key:"+key);
        String[] parts2 = args[1].split("=");

        if (parts2[0].contains("who")) {
            who = true;
        }

        rest = parts2[1];

//        如果key为null，则请求的格式不对
        if ( null == key){
            throw  new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
        }


//        根据id索索记录，获得Prediction对象
        Prediction p = predictions.getMap().get(new Integer(key.trim()));
        System.out.println("Predictions size:"+predictions.getMap().size());
        if ( null == p){


            String msg =  key + " does not map to a Prediction.\n";
            sendResponse(resp,msg,false);
        }else {
//            如果要更新的内容为null，则提示报错
            if ( null == rest){
                throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);

            }else {

                if (who) {
                    p.setWho(rest);

                }

//                提示更新成功
                String msg = "Prediction : {" + key + "} has been edited.\n";
                sendResponse(resp, predictions.toXML(msg), false);

            }

        }


    }

//    delete表示删除记录, postman中使用类似于get的方式，在url中传参数
    // DELETE /predictions2?id=1

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String param = req.getParameter("id");
        Integer key = (param == null) ? null: new Integer(param);

        if (null == key){
            throw  new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
        }

        System.out.println("predictions size before:"+predictions.getMap().size());
        predictions.getMap().remove(key);
        System.out.println("predictions size after delete:"+predictions.getMap().size());


        String msg = "Prediction " + key + " removed.\n";
        sendResponse(resp, predictions.toXML(msg), false);


    }

//    不允许Trace请求
    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }


    //不允许Options请求
    @Override
    public void doOptions(HttpServletRequest request, HttpServletResponse response) {
        throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }


//    返回响应到客户端
    private void sendResponse(HttpServletResponse resp, String payload, boolean json) throws IOException {

        System.out.println("sendResponse:");
        //是否转换为json
        if (json) {

            JSONObject jsonObject = XML.toJSONObject(payload);

//            将jsonObject转换为json字符串，缩进设置为3。如果是网络传输，则建议不缩进以节省流量
            payload = jsonObject.toString(3);
        }

            PrintWriter out = resp.getWriter();
            System.out.println("json:"+payload);

            out.write(payload);
            out.close();





    }
}
