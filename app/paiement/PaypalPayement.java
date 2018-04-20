package paiement;

import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Article;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import play.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import play.mvc.Result;
import utils.UtilsForm;

import java.net.URI;
import java.util.*;

import static play.mvc.Results.redirect;

/**
 * La classe qui permet de gérer les transactions entre votre projet et l'API Paypal.
 * La classe contient deux méthodes,
 * => La première envoyer l'ensemble des informations relative à la commande à l'API Paypal (appel en local sécuréser)
 * => redirection vers la page de paiement avec le token qui étais générer dernièrement
 */
public class PaypalPayement {



    private String username = "sm.2016paris_api1.gmail.com";
    private String password = "V2S3Q5YWGB733BAJ";
    private String signature = "ArDeC-FUgFX.vn-tYz-6Xk3bV02vAXHOfIMsTpMXRebX9DD0htOMi.A9";
    private String urlPaiement = "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&useraction=commit&token=";
    private String urlReturnUrl = "http://localhost:9001/paypal/urlReturnUrl";
    private String urlCancelUrl = "http://localhost:9001/paypal/urlCancelUrl";
    private String nvp = "https://api-3t.sandbox.paypal.com/nvp";





    /**
     * Methode qui permet d'envoyer l'ensemble des informations (user,pass, signature, urlSuccessPaiement,
     * urlCanselPaiement, plus l'ensemble des details sur les articles commander) et le webservice paypal
     * va returner un token qu'on va l'utiliser pour gérer le paiement
     * @param articles
     * @return
     */
    public String tokenPaiement(List<Article> articles){

        Map<String,String> listParams = new HashMap<>();
        String token = null;

        float total = 0;
        if(articles == null){
            return null;
        }



        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("USER",username));
        urlParameters.add(new BasicNameValuePair("PWD",password));
        urlParameters.add(new BasicNameValuePair("SIGNATURE",signature));
        urlParameters.add(new BasicNameValuePair("METHOD","SetExpressCheckout"));
        urlParameters.add(new BasicNameValuePair("VERSION","93"));
        urlParameters.add(new BasicNameValuePair("PAYMENTREQUEST_0_PAYMENTACTION","SALE"));

        urlParameters.add(new BasicNameValuePair("PAYMENTREQUEST_0_CURRENCYCODE","EUR"));
        urlParameters.add(new BasicNameValuePair("RETURNURL",urlReturnUrl));
        urlParameters.add(new BasicNameValuePair("CANCELURL",urlCancelUrl));
        urlParameters.add(new BasicNameValuePair("ALLOWNOTE","1"));

        int i = 0;
        for (Article article : articles) {
            total += article.getPrix();
            urlParameters.add(new BasicNameValuePair("L_PAYMENTREQUEST_0_NUMBER"+i,""+article.getId()));
            urlParameters.add(new BasicNameValuePair("L_PAYMENTREQUEST_0_NAME"+i,article.getNom()));
            urlParameters.add(new BasicNameValuePair("L_PAYMENTREQUEST_0_DESC"+i,""+article.getNom()));
            urlParameters.add(new BasicNameValuePair("L_PAYMENTREQUEST_0_AMT"+i, UtilsForm.floatToString(article.getPrix()).replace(",",".")));
            urlParameters.add(new BasicNameValuePair("L_PAYMENTREQUEST_0_QTY"+i,"1"));

            i++;
        }

        urlParameters.add(new BasicNameValuePair("PAYMENTREQUEST_0_SHIPDISCAMT","0"));
        urlParameters.add(new BasicNameValuePair("PAYMENTREQUEST_0_TAXAMT","0"));
        urlParameters.add(new BasicNameValuePair("PAYMENTREQUEST_0_AMT",UtilsForm.floatToString(total).replace(",",".")));
        urlParameters.add(new BasicNameValuePair("PAYMENTREQUEST_0_ITEMAMT",UtilsForm.floatToString(total).replace(",",".")));

        F.Promise<WS.Response> result = null;

        try {
            if(Play.isDev()){
                for (NameValuePair bean:urlParameters) {
                    System.out.println(bean.getName()+" : "+bean.getValue());
                }
            }

            result = WS.url(nvp).post(URLEncodedUtils.format(urlParameters, "utf-8"));

            String res = result.get(10000).getBody();

            System.out.println(res);

            List<NameValuePair> resList = URLEncodedUtils.parse(new URI("?"+res), "utf-8");


            if(resList != null && resList.size() != 0){
                for (NameValuePair bean:resList) {
                    listParams.put(bean.getName(),bean.getValue());

                    System.out.println(bean.getName()+"::::"+bean.getValue());

                    if("TOKEN".equals(bean.getName())){
                        token = bean.getValue();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return token;
    }

    /**
     * Methode appeler lorsque paypal valide la transaction
     * @param token
     * @param playId
     * @return
     */
    public String validePaiement(String token, String playId){

        ObjectNode objectNode = Json.newObject();


        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("USER",username));
        urlParameters.add(new BasicNameValuePair("PWD",password));
        urlParameters.add(new BasicNameValuePair("SIGNATURE",signature));
        urlParameters.add(new BasicNameValuePair("METHOD","GETExpressCheckoutDetails"));
        urlParameters.add(new BasicNameValuePair("VERSION","93"));
        urlParameters.add(new BasicNameValuePair("TOKEN",""+token));
        urlParameters.add(new BasicNameValuePair("PAYERID",""+playId));


        urlParameters.add(new BasicNameValuePair("PAYMENTREQUEST_0_PAYMENTACTION","SALE"));
        urlParameters.add(new BasicNameValuePair("ALLOWNOTE","1"));


        F.Promise<WS.Response> result = null;
        try {
            if(Play.isDev()){
                for (NameValuePair bean:urlParameters) {
                    System.out.println(bean.getName()+" : "+bean.getValue());
                }
            }

            result = WS.url(nvp).post(URLEncodedUtils.format(urlParameters, "utf-8"));
            String res = result.get(10000).getBody();

            List<NameValuePair> resList = URLEncodedUtils.parse(new URI(res), "utf-8");

            if(resList != null && resList.size() != 0){
                for (NameValuePair bean:resList) {
                    objectNode.put(bean.getName(),bean.getValue());
                    if(Play.isDev()){
                        System.out.println(bean.getName()+" : "+bean.getValue());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(objectNode.get("ACK") == null){
            return null;
        }

        if("Success".equals(objectNode.get("ACK").asText())){

            return "Success";
        }else{
            return objectNode.get("L_LONGMESSAGE0").asText();
        }
    }


    public String getUrlPaiement() {
        return urlPaiement;
    }


}
