package org.digitalmind.eventorchestrator.enumeration;

public enum EventActivityExecutionType {
    PARALLEL,               //NO DEPENDENCY ON EXECUTION
    SERIAL_PROCESS,         //SERIALIZE PA WITH THE SAME PROCESS ID
    SERIAL_ENTITY
    //,          //SERIALIZE PA WITH THE SAME PROCESS ID, ENTITY ID AND ENTITY NAME
    //SERIAL_TYPE             //SERIALIZE PA WITH THE SAME PROCESS ID, ENTITY ID AND ENTITY NAME AND TYPE

}
