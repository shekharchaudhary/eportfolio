################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/CSVparser.cpp \
../src/VectorSorting.cpp 

CPP_DEPS += \
./src/CSVparser.d \
./src/VectorSorting.d 

OBJS += \
./src/CSVparser.o \
./src/VectorSorting.o 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.cpp src/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-src

clean-src:
	-$(RM) ./src/CSVparser.d ./src/CSVparser.o ./src/VectorSorting.d ./src/VectorSorting.o

.PHONY: clean-src

