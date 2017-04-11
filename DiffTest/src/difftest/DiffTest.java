/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package difftest;

import com.perago.techtest.Diff;
import com.perago.techtest.DiffException;
import com.perago.techtest.test.Person;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jeanpe
 */
public class DiffTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Unit testing
        Person tom = new Person();
        Person dick = new Person();
        Person harry = new Person();
        Person susan = new Person();
        Person june = new Person();
        Person carrol = new Person();

        HashSet<String> nicknamesTom = new HashSet<>(Arrays.asList("Tomee", "Tomson", "Thomas"));
        HashSet<String> nicknamesDick = new HashSet<>(Arrays.asList("Dicky", "Dickson", "Rich", "Richard"));
        HashSet<String> nicknamesHarry = new HashSet<>(Arrays.asList("Potter", "TheNameToBeSpoken"));

        HashSet<String> nicknamesSusan = null;
        HashSet<String> nicknamesJune = new HashSet<>(Arrays.asList("July", "February", "Pearl"));
        HashSet<String> nicknamesCarrol = new HashSet<>(Arrays.asList("Christmas", "Easter"));

        tom.setFirstName("Tom");
        tom.setNickNames(nicknamesTom);
        tom.setSurname("SurnameTom");
        tom.setFriend(dick);

        dick.setFirstName("Dick");
        dick.setNickNames(nicknamesDick);
        dick.setSurname("Dickson");
        dick.setFriend(tom);

        harry.setFirstName("Harry");
        harry.setNickNames(nicknamesHarry);
        harry.setSurname("Potter");
        harry.setFriend(susan);

        susan.setFirstName("Susan");
        susan.setNickNames(nicknamesSusan);
        susan.setSurname("Cooper");
        susan.setFriend(june);

        june.setFirstName("June");
        june.setNickNames(nicknamesJune);
        june.setSurname("Joyce");
        june.setFriend(null);

        carrol.setFirstName("Carrol");
        carrol.setNickNames(nicknamesCarrol);
        carrol.setSurname("Potter");
        carrol.setFriend(null);

        DiffEngineImp diffTest = new DiffEngineImp();
        DiffRenderImp difRenderTest = new DiffRenderImp();

        try {
            //Diff tom and harry
            Diff<Person> newDiff = diffTest.calculate(null, tom);
            System.out.println("null to tom - tom is friends with dick that is friends with tom ");            
            System.out.println(difRenderTest.render(newDiff));

            //Create harry from tom and diff
            System.out.println("Apply Diff to null - Should Become Tom");            
            Person modPerson = diffTest.apply(tom, newDiff);
            System.out.println("");   
            
            //Check new Tom obj
            System.out.println("New Tom Object");   
            System.out.println("FistName is " + modPerson.getFirstName());
            System.out.println("SurName is " + modPerson.getSurname());
            System.out.println("NickNames is " + modPerson.getNickNames());
            System.out.println("Friend Obj is " + modPerson.getFriend().getFirstName());

            if (modPerson.getFriend() != null) {
                System.out.println("\tFistName is " + modPerson.getFriend().getFirstName());
                System.out.println("\tSurName is " + modPerson.getFriend().getSurname());
                System.out.println("\tNickNames is " + modPerson.getFriend().getNickNames());
                System.out.println("\tFriend Obj is " + modPerson.getFriend().getFriend().getFirstName());
            }
            
            System.out.println("");   
            //Same 
            System.out.println("Harry to Harry");            
            newDiff = diffTest.calculate(harry, harry);
            System.out.println(difRenderTest.render(newDiff));

            //Dick to harry
            System.out.println("Dick to Harry");            
            newDiff = diffTest.calculate(dick, harry);
            System.out.println(difRenderTest.render(newDiff));

            //null to susan
            System.out.println("null to Susan");            
            newDiff = diffTest.calculate(null, susan);
            System.out.println(difRenderTest.render(newDiff));

            //carrol to null
            System.out.println("Carrol to Null");            
            newDiff = diffTest.calculate(carrol, null);
            System.out.println(difRenderTest.render(newDiff));

            //june to susan
            System.out.println("Susan to June");            
            newDiff = diffTest.calculate(susan, june);
            System.out.println(difRenderTest.render(newDiff));

            //susan to june
            System.out.println("June to Susan");            
            newDiff = diffTest.calculate(june, susan);
            System.out.println(difRenderTest.render(newDiff));

        } catch (DiffException ex) {
            Logger.getLogger(DiffTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
