public class ObjectWithValue implements Comparable<ObjectWithValue> {

	public Object o;
	public double value;
	
	public ObjectWithValue(Object o,double value) {
		this.o = o;
		this.value = value;
	}

	@Override
	public int compareTo(ObjectWithValue o) {
		double t1 = Math.abs(value);
		double t2 = Math.abs(o.value);
		if(t1 < t2) return -1;
		if(t1 > t2) return 1;
		return 0;
	}

	@Override
	public String toString() {
		return "[" + o + ", value=" + value + "]";
	}

}
