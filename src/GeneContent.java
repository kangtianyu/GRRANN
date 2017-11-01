
public class GeneContent {
	
	public String name;
	public String uid;
	public String type;

	public GeneContent(String name,String uid,String type){
		this.name = name;
		this.uid = uid;
		this.type = type;
	}
	
	@Override
	public String toString(){
		return name + " " + uid + " " + type;
	}
}
