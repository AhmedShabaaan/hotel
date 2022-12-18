import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Hotel {
	
	public static void main (String a[]) throws InterruptedException {	
		
		int noOfBarbers=2, customerId=1, noOfCustomers=100, noOfChairs;	
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Enter the number of receptionists:");			
    	noOfBarbers=sc.nextInt();
    	
    	System.out.println("Enter the number of "			
    			+ " rooms:");
    	noOfChairs=sc.nextInt();
    	
    	System.out.println("Enter the number of guests:");			
    	noOfCustomers=sc.nextInt();
    	
		ExecutorService exec = Executors.newFixedThreadPool(12);		
    	Rooms shop = new Rooms(noOfBarbers, noOfChairs);				
    	Random r = new Random();  										
       	    	
        System.out.println("\nHotel opened with "
        		+noOfBarbers+" receptionist(s)\n");
        
        long startTime  = System.currentTimeMillis();					
        
        for(int i=1; i<=noOfBarbers;i++) {								
        	
        	Receptionist barber = new Receptionist(shop, i);	
        	Thread thbarber = new Thread(barber);
            exec.execute(thbarber);
        }
        
        for(int i=0;i<noOfCustomers;i++) {								
        
            Guest customer = new Guest(shop);
            customer.setInTime(new Date());
            Thread thcustomer = new Thread(customer);
            customer.setcustomerId(customerId++);
            exec.execute(thcustomer);
            
            try {
            	
            	double val = r.nextGaussian() * 2000 + 2000;				
            	int millisDelay = Math.abs((int) Math.round(val));		
            	Thread.sleep(millisDelay);								
            }
            catch(InterruptedException iex) {
            
                iex.printStackTrace();
            }
            
        }
        
        exec.shutdown();												
        exec.awaitTermination(12, SECONDS);								
 
        long elapsedTime = System.currentTimeMillis() - startTime;		
        
        System.out.println("\nHotel closed");
        System.out.println("\nTotal time elapsed in seconds"
        		+ " for serving "+noOfCustomers+" guests by "
        		+noOfBarbers+" receptionists with "+noOfChairs+
        		" rooms in the waiting room is: "
        		+TimeUnit.MILLISECONDS
        	    .toSeconds(elapsedTime));
        System.out.println("\nTotal Guests: "+noOfCustomers+
        		"\nTotal Guests served: "+shop.getTotalHairCuts()
        		+"\nTotal Guests lost: "+shop.getCustomerLost());
               
        sc.close();
    }
}
 
class Receptionist implements Runnable {										

    Rooms shop;
    int barberId;
 
    public Receptionist(Rooms shop, int barberId) {
    
        this.shop = shop;
        this.barberId = barberId;
    }
    
    public void run() {
    
        while(true) {
        
            shop.cutHair(barberId);
        }
    }
}

class Guest implements Runnable {

    int customerId;
    Date inTime;
 
    Rooms shop;
 
    public Guest(Rooms shop) {
    
        this.shop = shop;
    }
 
    public int getCustomerId() {										
        return customerId;
    }
 
    public Date getInTime() {
        return inTime;
    }
 
    public void setcustomerId(int customerId) {
        this.customerId = customerId;
    }
 
    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }
 
    public void run() {													
    
        goForHairCut();
    }
    private synchronized void goForHairCut() {							
    
        shop.add(this);
    }
}
 
class Rooms {

	private final AtomicInteger totalHairCuts = new AtomicInteger(0);
	private final AtomicInteger customersLost = new AtomicInteger(0);
	int nchair, noOfBarbers, availableBarbers;
    List<Guest> listCustomer;
    
    Random r = new Random();	 
    
    public Rooms(int noOfBarbers, int noOfChairs){
    
        this.nchair = noOfChairs;														
        listCustomer = new LinkedList<Guest>();						
        this.noOfBarbers = noOfBarbers;									
        availableBarbers = noOfBarbers;
    }
 
    public AtomicInteger getTotalHairCuts() {
    	
    	totalHairCuts.get();
    	return totalHairCuts;
    }
    
    public AtomicInteger getCustomerLost() {
    	
    	customersLost.get();
    	return customersLost;
    }
    
    public void cutHair(int barberId)
    {
        Guest customer;
        synchronized (listCustomer) {									
        															 	
            while(listCustomer.size()==0) {
            
                System.out.println("\nreceptionist "+barberId+" is waiting "
                		+ "for the Guest");
                
                try {
                
                    listCustomer.wait();								
                }
                catch(InterruptedException iex) {
                
                    iex.printStackTrace();
                }
            }
            
            customer = (Guest)((LinkedList<?>)listCustomer).poll();	
            
            System.out.println("Guest "+customer.getCustomerId()+
            		" finds the receptionist "
            		+barberId);
        }
        
        int millisDelay=0;
                
        try {
        	
        	availableBarbers--; 										
        																
            System.out.println("receptionist "+barberId+" serving Guest "+
            		customer.getCustomerId());
        	
            double val = r.nextGaussian() * 2000 + 4000;				
        	millisDelay = Math.abs((int) Math.round(val));				
        	Thread.sleep(millisDelay);
        	
        	System.out.println("\nCompleted Booking "+
        			customer.getCustomerId()+" by receptionist " + 
        			barberId +" in "+millisDelay+ " milliseconds.");
        
        	totalHairCuts.incrementAndGet();
            															
            if(listCustomer.size()>0) {									
            	System.out.println("receptionist "+barberId+					
            			" serving a Guest "					
            			);		
            }
            
            availableBarbers++;											
        }
        catch(InterruptedException iex) {
        
            iex.printStackTrace();
        }
        
    }
 
    public void add(Guest customer) {
    
        System.out.println("\nGuest "+customer.getCustomerId()+
        		" enters through the entrance door in the the Hotel at "
        		+customer.getInTime());
 
        synchronized (listCustomer) {
        
            if(listCustomer.size() == nchair) {							
            
                System.out.println("\nNo room available "
                		+ "for guest "+customer.getCustomerId()+
                		" so guest leaves the hotel");
                
              customersLost.incrementAndGet();
                
                return;
            }
            else if (availableBarbers > 0) {							
            															
            	((LinkedList<Guest>)listCustomer).offer(customer);
				listCustomer.notify();
			}
            else {														
            															
            	((LinkedList<Guest>)listCustomer).offer(customer);
                
            	System.out.println("All receptionist(s) are busy so "+
            			customer.getCustomerId()+
                		" takes a chair in the waiting room");
                 
                if(listCustomer.size()==1)
                    listCustomer.notify();
            }
        }
    }
}

