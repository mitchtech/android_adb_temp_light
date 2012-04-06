#include <SPI.h>
#include <Adb.h>

// Adb connection.
Connection * connection;

// Elapsed time for sensor sampling
long lastTime;

// Event handler for shell connection; called whenever data sent from Android to Microcontroller
void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
  // Unused in this case
}

void setup()
{
  Serial.begin(57600);

  // Record time for sensor polling timer
  lastTime = millis();

  // Init the ADB subsystem.
  ADB::init();

  // Open an ADB stream to the phone's shell. Auto-reconnect. Use port number 4568
  connection = ADB::addConnection("tcp:4568", true, adbEventHandler);  

  Serial.println("Ready!");
}

void loop()
{
  //Check if sensor should be sampled.
  if ((millis() - lastTime) > 20)
  {
    uint16_t data[2];
    // light sensor
    data[0] = analogRead(A0);
    float celsius = getVoltage(1);  //getting the voltage reading from the temp sensor
    celsius = (celsius - .5) * 100;          //converting from 10 mv per degree wit 500 mV offset
                                                   //to degrees ((volatge - 500mV) times 100)
    float fahrenheit = (celsius * (9.0 / 5.0)) + 32.0;
    data[1] = (int) fahrenheit;

    //Send the sensor value to Android as 4 bytes of data.
    connection->write(sizeof(data),(uint8_t*)&data);

    // Output debugging to serial
    Serial.println(data[0],DEC);
    Serial.println(data[1],DEC);

    // Update timer for sensor check
    lastTime = millis();
  }

  // Poll the ADB subsystem.
  ADB::poll();
}

float getVoltage(int pin){
  return (analogRead(pin) * .004882814); //converting from a 0 to 1024 digital range
  // to 0 to 5 volts (each 1 reading equals ~ 5 millivolts
}

