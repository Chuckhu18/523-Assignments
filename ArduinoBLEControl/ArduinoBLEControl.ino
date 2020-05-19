/*********************************************************************
  * Laura Arjona. UW EE P 523. SPRING 2020
  * Example of simple interaction beteween Adafruit Circuit Playground
  * and Android App. Communication with BLE - uart
  * Chuck Hu. Assignment5
*********************************************************************/
#include <Arduino.h>
#include <SPI.h>
#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"
#include <Adafruit_CircuitPlayground.h>

#include "BluefruitConfig.h"

#if SOFTWARE_SERIAL_AVAILABLE
  #include <SoftwareSerial.h>
#endif


// Strings to compare incoming BLE messages
String start = "start";
String red = "red";

String readtemp = "readtemp";
String stp = "stop";
String cancel = "cancel";

int  sensorTemp = 0;

int SAMPLE_NUM = 50;
float OPEN_THRESHOLD = 0.19;
const int speaker = 5;       // The CP microcontroller pin for the speaker
const int leftButton = 4;    // The CP microcontroller pin for the left button
const int rightButton = 19;  // The CP microcontroller pin for the right button


float X,Y,Z;
float alpha = 0.8;
float gravity[3] = {0,0,0};
float linear_accel[3] = {0,0,0};
int counter = 0;
float mag_sum=0;
float mag_avg;

bool init_count = false;
bool save_recvd = false;
bool alarm_mode = false;

bool alarm_status = false;
bool sent = false;
bool reset_alarm = false;


float timer1;
float timer2;

int blink_trig = 0;
String recvd = "";


/*=========================================================================
    APPLICATION SETTINGS
    -----------------------------------------------------------------------*/
    #define FACTORYRESET_ENABLE         0
    #define MINIMUM_FIRMWARE_VERSION    "0.6.6"
    #define MODE_LED_BEHAVIOUR          "MODE"
/*=========================================================================*/

// Create the bluefruit object, either software serial...uncomment these lines

Adafruit_BluefruitLE_UART ble(BLUEFRUIT_HWSERIAL_NAME, BLUEFRUIT_UART_MODE_PIN);

/* ...hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST */
// Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

/* ...software SPI, using SCK/MOSI/MISO user-defined SPI pins and then user selected CS/IRQ/RST */
//Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_SCK, BLUEFRUIT_SPI_MISO,
//                             BLUEFRUIT_SPI_MOSI, BLUEFRUIT_SPI_CS,
//                             BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);


// A small helper to show errors on the serial monitor
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}


void setup(void)
{
  CircuitPlayground.begin();
  
  Serial.begin(115200);
  
  pinMode(speaker, OUTPUT);
  pinMode(leftButton, INPUT);
  pinMode(rightButton,INPUT);

  
  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );

  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      error(F("Couldn't factory reset"));
    }
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();

  Serial.println(F("Please use Adafruit Bluefruit LE app to connect in UART mode"));
  Serial.println(F("Then Enter characters to send to Bluefruit"));
  Serial.println();

  ble.verbose(false);  // debug info is a little annoying after this point!

  /* Wait for connection */
  while (! ble.isConnected()) {
      delay(500);
  }
Serial.println("CONECTED:");
  Serial.println(F("******************************"));

  // LED Activity command is only supported from 0.6.6
  if ( ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
  {
    // Change Mode LED Activity
    Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
  }

  // Set module to DATA mode
  Serial.println( F("Switching to DATA mode!") );
  ble.setMode(BLUEFRUIT_MODE_DATA);

  Serial.println(F("******************************"));
  CircuitPlayground.setPixelColor(20,20,20,20);
  
  for(int i=0; i<10; i++){
    CircuitPlayground.setPixelColor(i,0,255,0);
    delay(50);
  }
  delay(1000);
}
/**************************************************************************/
/*!
   Constantly poll for new command or response data
*/
/**************************************************************************/
void loop(void)
{
  // Save received data to string
  String received = "";
  while ( ble.available() )
  {
    int c = ble.read();
    Serial.print((char)c);
    received += (char)c;
    delay(50);
    Serial.println("In while loop");
  }

  // Alarm mode activated
  while(alarm_mode){
    Serial.println("Alarm Mode");
    reset_alarm = false;
    
    if(blink_trig % 2 == 0){
      for(int i=0; i<10; i++)
        CircuitPlayground.setPixelColor(i,255,0,0);
        makeTone(speaker,1760,100);
    }else{ 
      CircuitPlayground.clearPixels();
    }
    
    //If one button is pressed, and another button 
    //is pressed in 2 second, alarm will be reset
    if(digitalRead(leftButton)){
      timer1 = millis();
      while((timer2 - timer1) <= 2000){
        timer2 = millis();
        if(digitalRead(rightButton))
          reset_alarm = true;
      }
    }
    if(digitalRead(rightButton)){
      timer1 = millis();
      while((timer2 - timer1) <= 2000){
        timer2 = millis();
        if(digitalRead(leftButton))
          reset_alarm = true;
      }
    }
    
    if(reset_alarm == true){
      sent = false;
      alarm_status = false;
      save_recvd = false;
      alarm_mode = false;
      init_count = false;
      
      for(int i=0; i<10; i++)
        CircuitPlayground.setPixelColor(i,0,255,0);
      
      break;
    }
     
    blink_trig++;
    delay(500);
  }

  if(save_recvd == false && received != ""){
    recvd = received;
    save_recvd = true;
    Serial.println(recvd);
  }

  if(received != "" || recvd !="" ){
    Serial.print("received:");
    Serial.print(received);
    Serial.print("recvd:");
    Serial.println(recvd);
    }

  //gravity cancelation
  X = CircuitPlayground.motionX();
  Y = CircuitPlayground.motionY();
  Z = CircuitPlayground.motionZ();
  gravity[0] = alpha * gravity[0] + (1 - alpha) * X;
  gravity[1] = alpha * gravity[1] + (1 - alpha) * Y;
  gravity[2] = alpha * gravity[2] + (1 - alpha) * Z;
  linear_accel[0] = X - gravity[0];
  linear_accel[1] = Y - gravity[1];
  linear_accel[2] = Z - gravity[2];

  //calculate magnitude of x and z, since y axis is not moving.
  float mag = sqrt(pow(linear_accel[0],2)+pow(linear_accel[2],2));

  delay(10);
  
  if(counter>=SAMPLE_NUM){
    counter = 0;
    mag_avg = mag_sum/SAMPLE_NUM;
//    Serial.print("mag: ");
//    Serial.println(mag_avg);
    mag_sum = 0;
    
    if(mag_avg >= OPEN_THRESHOLD && init_count == true && alarm_status == false){
      timer1 = millis();
      alarm_status = true;
    }

    if(alarm_status){
        if(blink_trig % 2 == 0){
          for(int i=0; i<10; i++){
            CircuitPlayground.setPixelColor(i,255,255,0);
          }
        }else{
            CircuitPlayground.clearPixels();
        } 
      blink_trig++;
      
//      Serial.print("Received:");
//      Serial.println(recvd);
      alarmTriggered(recvd);
    }
    init_count = true;
  }
  else{
    mag_sum += mag;
    counter++;
  }
}
  
void alarmTriggered(String received){
//  Serial.println("Door Opened!!!!");
  String data = "doorOpened";
  
  if(sent == false){
    Serial.println("sent = true");
    ble.print(data);
    sent = true;
  }
    
  timer2 = millis();

//  Serial.print("********DEBUG:");
//  Serial.println(received);
  
  if(cancel == received){// release alarm
    Serial.println("cancel received");
    sent = false;
    alarm_status = false;
    save_recvd = false;
    for(int i=0; i<10; i++){
      CircuitPlayground.setPixelColor(i,0,255,0);
    }
    recvd = "";
  }
  
  if((timer2-timer1) >= 10000 && cancel != received){
    alarm_mode = true;
    data = "noResponse";
    ble.print(data);

//    alarm_status = true;
  }
}

// the sound producing function (a brute force way to do it)
void makeTone (unsigned char speakerPin, int frequencyInHertz, long timeInMilliseconds) {
  int x;   
  long delayAmount = (long)(1000000/frequencyInHertz);
  long loopTime = (long)((timeInMilliseconds*1000)/(delayAmount*2));
  for (x=0; x<loopTime; x++) {        // the wave will be symetrical (same time high & low)
     digitalWrite(speakerPin,HIGH);   // Set the pin high
     delayMicroseconds(delayAmount);  // and make the tall part of the wave
     digitalWrite(speakerPin,LOW);    // switch the pin back to low
     delayMicroseconds(delayAmount);  // and make the bottom part of the wave
  }  
}

 
