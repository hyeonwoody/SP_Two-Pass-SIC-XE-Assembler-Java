import java.util.ArrayList;
import java.lang.StackTraceElement;

/**
 * symbol, literal과 관련된 데이터와 연산을 소유한다. section 별로 하나씩 인스턴스를 할당한다.
 */
public class LabelTable {
	ArrayList<String> label;
	ArrayList<Integer> locationList;
	// external 선언 및 처리방법을 구현한다.
	
	public LabelTable(){
		label = new ArrayList<>();
		locationList = new ArrayList<Integer>();
	}
	/**
	 * 새로운 symbol과 literal을 table에 추가한다.
	 * 
	 * @param label    : 새로 추가되는 symbol 혹은 literal의 lable
	 * @param location : 해당 symbol 혹은 literal이 가지는 주소값 주의 : 만약 중복된 symbol, literal이
	 *                 putName을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 매칭되는 주소값의 변경은
	 *                 modifylable()을 통해서 이루어져야 한다.
	 */
	public void putName(String label, int location) {
		if (!(this.label.contains(label))) {
			this.label.add(label);
			this.locationList.add(location);
		}
		else 
			System.out.println ("Already Exist : "+label+location);
	}

	/**
	 * 기존에 존재하는 symbol, literal 값에 대해서 가리키는 주소값을 변경한다.
	 * 
	 * @param lable       : 변경을 원하는 symbol, literal의 label
	 * @param newLocation : 새로 바꾸고자 하는 주소값
	 */
	public void modifyName(String lable, int newLocation) {
		locationList.set(label.indexOf(lable), newLocation);
	}

	/**
	 * 인자로 전달된 symbol, literal이 어떤 주소를 지칭하는지 알려준다.
	 * 
	 * @param label : 검색을 원하는 symbol 혹은 literal의 label
	 * @return address: 가지고 있는 주소값. 해당 symbol, literal이 없을 경우 -1 리턴
	 */
	public int search(String label) {
		int address = 0;
		// ...
		int index;
		if ((index = this.label.indexOf(label)) == -1) {
			return index;
		}
		else {
			address = this.locationList.get(index);
		}
		return address;
	}

}
