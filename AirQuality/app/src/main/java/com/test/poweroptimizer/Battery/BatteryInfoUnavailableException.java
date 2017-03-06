/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 COPYRIGHT (c) 2016   HONEYWELL INC.,  ALL RIGHTS RESERVED
 
 This software is a copyrighted work and/or information protected as a trade secret. 
 Legal rights of Honeywell Inc. in this software  is distinct from ownership of any 
 medium in which the software is embodied. Copyright or trade secret notices included 
 must be reproduced in any copies authorized by Honeywell Inc. 
 The information in this software is subject to change without notice and should not 
 be considered as a commitment by Honeywell Inc.

//  @ Project : DAC
//  @ Module  : Device Engine 
//  @ Component : Battery 
//  @ File Name : BatteryInfoUnavailableException.java
//  @ Date : 29/08/2016
//  @ Author(s) : Sand(E547883)
//
//
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- */

package com.test.poweroptimizer.Battery;

public class BatteryInfoUnavailableException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public BatteryInfoUnavailableException() {

    }

    public BatteryInfoUnavailableException(String msg) {
        super(msg);
    }
}
