/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author SML-DEV-PC9
 */
public class myglobal {

    public String _calcFormulaPrice(String qty, String price, String formula) {
        String _newPrice = price;
        if (formula.trim().length() > 0) {
            // = = กำหนดราคาขาย
            // + = เพิ่ม
            // - = ลด
            if (String.valueOf(formula.charAt(0)).equals("=") || String.valueOf(formula.charAt(0)).equals("-") || String.valueOf(formula.charAt(0)).equals("+")) {
                if (String.valueOf(formula.charAt(0)).equals("=")) {
                    String[] __split = formula.split(",");
                    if (__split.length > 0) {
                        //เปลี่ยนราคาใหม่
                        String _priceStr = __split[0].replace("=", "");
                        _newPrice = _priceStr;
                        StringBuilder __newFormat = new StringBuilder();
                        for (int _loop = 1; _loop < __split.length; _loop++) {
                            if (__newFormat.length() > 0) {
                                __newFormat.append(",");
                            }
                            __newFormat.append(__split[_loop]);
                        }
                        formula = __newFormat.toString();
                    }
                } else if (formula.charAt(0) == '-') {
                    String[] __split = formula.split(",");
                    if (__split.length > 0) {
                        //เปลี่ยนราคาใหม่
                        String _priceStr = __split[0].replace("-", "");
                        Double newprice = Double.parseDouble(_newPrice) - Double.parseDouble(_priceStr);
                        _newPrice = newprice.toString();
                        StringBuilder __newFormat = new StringBuilder();
                        for (int _loop = 1; _loop < __split.length; _loop++) {
                            if (__newFormat.length() > 0) {
                                __newFormat.append(",");
                            }
                            __newFormat.append(__split[_loop]);
                        }
                        formula = __newFormat.toString();
                    }
                } else if (formula.charAt(0) == '+') {
                    String[] __split = formula.split(",");
                    if (__split.length > 0) {
                        //เปลี่ยนราคาใหม่
                        String _priceStr = __split[0].replace("+", "");
                        Double newprice = Double.parseDouble(_newPrice) + Double.parseDouble(_priceStr);
                        _newPrice = newprice.toString();
                        StringBuilder __newFormat = new StringBuilder();
                        for (int _loop = 1; _loop < __split.length; _loop++) {
                            if (__newFormat.length() > 0) {
                                __newFormat.append(",");
                            }
                            __newFormat.append(__split[_loop]);
                        }
                        formula = __newFormat.toString();
                    }
                }
            } else {
                _newPrice = formula;

            }
        } else {
            _newPrice = price;
        }

        return _newPrice;
    }

}
