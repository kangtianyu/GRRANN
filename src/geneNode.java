
public class geneNode {
	
	private int id;
	private String name;
	private int proteinUID;
	private int mrnaUID;

	public geneNode(int id,String name){
		this.id = id;
		this.name = name;
		this.proteinUID = -1;
		this.mrnaUID = -1;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getProteinUID() {
		return proteinUID;
	}

	public void setProteinUID(int proteinUID) {
		this.proteinUID = proteinUID;
	}

	public int getMrnaUID() {
		return mrnaUID;
	}

	public void setMrnaUID(int mrnaUID) {
		this.mrnaUID = mrnaUID;
	}

	@Override
	public String toString() {
		return "geneNode [id=" + id + ", name=" + name + ", proteinUID="
				+ proteinUID + ", mrnaUID=" + mrnaUID + "]";
	}
}
