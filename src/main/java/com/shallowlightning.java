package com;
// Marker Annotation
// Single value annotation
// multi value annotation
@interface  SmartPhone
{
    String os() default "Symbian";
    int version() default 1;
}

@SmartPhone(os="Android", version=6)
class NokiaASeries{

}
public class shallowlightning {
    public static void main(){

    }
}
