//
// Copyright (c) ZeroC, Inc. All rights reserved.
//
//
// Ice version 3.7.10
//
// <auto-generated>
//
// Generated from file `smarthome.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package Smarthome;

public class AboveMaxTemperature extends com.zeroc.Ice.UserException
{
    public AboveMaxTemperature()
    {
        this.message = "";
    }

    public AboveMaxTemperature(Throwable cause)
    {
        super(cause);
        this.message = "";
    }

    public AboveMaxTemperature(String message)
    {
        this.message = message;
    }

    public AboveMaxTemperature(String message, Throwable cause)
    {
        super(cause);
        this.message = message;
    }

    public String ice_id()
    {
        return "::Smarthome::AboveMaxTemperature";
    }

    public String message;

    /** @hidden */
    @Override
    protected void _writeImpl(com.zeroc.Ice.OutputStream ostr_)
    {
        ostr_.startSlice("::Smarthome::AboveMaxTemperature", -1, true);
        ostr_.writeString(message);
        ostr_.endSlice();
    }

    /** @hidden */
    @Override
    protected void _readImpl(com.zeroc.Ice.InputStream istr_)
    {
        istr_.startSlice();
        message = istr_.readString();
        istr_.endSlice();
    }

    /** @hidden */
    public static final long serialVersionUID = 811349206L;
}
