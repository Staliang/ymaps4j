package com.staliang.ymaps4j;

import com.staliang.ymaps4j.json.types.Route;

/**
 * Created by Alexandr_Badin on 11.08.2015.
 */
public interface YMaps {

    /**
    * ����������� �������� ������� �� ��� �����������
     * @param coordinate
    * @return
            */
    String geocode(Coordinate coordinate) throws YMapsException;

    /**
     * ����������� ���������� ������� �� ��� ��������
     * @param location
     * @return
     */
    Coordinate geocode(String location) throws YMapsException;

    /**
     * ��������� ������� �� ��������� ����� ��������
     * @param locations
     * @return
     * @throws YMapsException
     */
    Route route(String... locations) throws YMapsException;

    /**
     * ��������� ������� �� ����������� ����� ��������
     * @param coordinates
     * @return
     * @throws YMapsException
     */
    Route route(Coordinate... coordinates) throws YMapsException;
}
