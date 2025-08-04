/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simple;

/**
 *
 * @author user
 */
public class Test {
    void main(){
        Test.<String>identity(String.class, "Joe");
    }
    public static <T> T identity(T value) {
        return value;
    }
    
    public static <T> T identity(Class<T> clazz, T value) {
        return value;
    }
}
