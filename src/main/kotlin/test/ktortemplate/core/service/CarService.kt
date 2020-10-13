package test.ktortemplate.core.service

import test.ktortemplate.core.model.Car
import test.ktortemplate.core.model.CarSaveCommand
import test.ktortemplate.core.model.RegisterPartReplacementCommand

interface CarService {
    fun getCarById(carId: Long): Car?
    fun insertNewCar(newCar: CarSaveCommand): Car

    fun registerPartReplacement(replacedParts: RegisterPartReplacementCommand): Car
}
