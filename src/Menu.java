import java.util.ArrayList;

public class Menu {

	private String message;
	private int optionCount;
	private ArrayList<Runnable> menuOptions = new ArrayList<>();

	public Menu(String message,optionCount) {
		this.message = message;
		this.optionCount = optionCount;
	}

	public void run() {
		System.out.print(message);	
	}

	public void setOption(int index, Runnable optionMethod) {
		menuOptions[index] = optionMethod;
		
	}

}
