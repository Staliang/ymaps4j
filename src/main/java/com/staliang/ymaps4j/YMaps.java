package com.staliang.ymaps4j;

import com.staliang.ymaps4j.json.types.Route;

/**
 * Created by Alexandr_Badin on 11.08.2015.
 */
public interface YMaps {

    /**
    * ����������� �������� ������� �� ��� �����������
    * @param point
    * @return
            */
    String geocode(Point point) throws YMapsException;

    /**
     * ����������� ���������� ������� �� ��� ��������
     * @param location
     * @return
     */
    Point geocode(String location) throws YMapsException;

    /**
     * ��������� ������� �� ��������� ����� ��������
     * @param locations
     * @return
     * @throws YMapsException
     */
    Route route(String... locations) throws YMapsException;

    /**
     * ��������� ������� �� ����������� ����� ��������
     * @param points
     * @return
     * @throws YMapsException
     */
    Route route(Point... points) throws YMapsException;
}
