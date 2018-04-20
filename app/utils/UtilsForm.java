package utils;


/**
 * Classe qui contient l'ensemble des méthodes utils pour notre projet
 */
public class UtilsForm
{

	
	public static float getFloatByString(String data){
		if(data == null || data.length() == 0){
			return 0;
		}
		
		try{
			return Float.parseFloat(data.replace(",", "."));
		}catch(Exception ex){
			
		}
		return 0;
	}

	public static String floatToString(float value)
	{
		return String.format("%.2f", value);
	}

}
