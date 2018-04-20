package controllers;

import model.Article;
import paiement.PaypalPayement;
import play.data.DynamicForm;
import play.mvc.*;

import views.html.*;


import java.util.*;

import static play.data.Form.form;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }


    public static Result paypal(){

        PaypalPayement payement = new PaypalPayement();
        String token = payement.tokenPaiement(getListArticle());

        if(token == null){
            return ok(views.html.paypalPage.render("Paiement invalide (MSG : 001)", ""));
        }

        return redirect(payement.getUrlPaiement()+token);
    }


    public static Result urlReturnUrl(){

        String token, playId;

        DynamicForm form = form().bindFromRequest();
        if (form.get("token") != null && form.get("PayerID") != null) {
            token = form.get("token");
            playId = form.get("PayerID");
        }else{

            return ok(views.html.paypalPage.render("Erreur du paiement", "Paramètres non valide"));
        }



        PaypalPayement payement = new PaypalPayement();
        String t = payement.validePaiement(token,playId );
        if("Success".equals(t)){
            return ok(views.html.paypalPage.render("Paiement effectuée", ""));
        }else{
            return ok(views.html.paypalPage.render("Erreur du paiement", ""));

        }

    }

    public static Result urlCancelUrl(){

        return ok(views.html.paypalPage.render("Paiement annuler", ""));
    }


    private static List<Article> getListArticle(){
        List<Article> list = new ArrayList<Article>();

        list.add(new Article(1, "Produit 1", 34));

        return list;
    }


}
