#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <cstring>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <iostream>

//=========================VICON specific=========================
#include "Client.h"

#include <iostream>
#include <fstream>
#include <cassert>
#include <ctime>

#ifdef WIN32
#include <conio.h>   // For _kbhit()
#include <cstdio>   // For getchar()
#include <windows.h> // For Sleep()
#endif // WIN32

#include <time.h>
#include <unistd.h>

using namespace ViconDataStreamSDK::CPP;

#define output_stream if(!LogFile.empty()) ; else std::cout 

namespace
{
    std::string Adapt( const bool i_Value )
    {
        return i_Value ? "True" : "False";
    }

    std::string Adapt( const Direction::Enum i_Direction )
    {
        switch( i_Direction )
        {
            case Direction::Forward:
                return "Forward";
            case Direction::Backward:
                return "Backward";
            case Direction::Left:
                return "Left";
            case Direction::Right:
                return "Right";
            case Direction::Up:
                return "Up";
            case Direction::Down:
                return "Down";
            default:
                return "Unknown";
        }
    }

    std::string Adapt( const DeviceType::Enum i_DeviceType )
    {
        switch( i_DeviceType )
        {
            case DeviceType::ForcePlate:
                return "ForcePlate";
            case DeviceType::Unknown:
            default:
                return "Unknown";
        }
    }

    std::string Adapt( const Unit::Enum i_Unit )
    {
        switch( i_Unit )
        {
            case Unit::Meter:
                return "Meter";
            case Unit::Volt:
                return "Volt";
            case Unit::NewtonMeter:
                return "NewtonMeter";
            case Unit::Newton:
                return "Newton";
            case Unit::Kilogram:
                return "Kilogram";
            case Unit::Second:
                return "Second";
            case Unit::Ampere:
                return "Ampere";
            case Unit::Kelvin:
                return "Kelvin";
            case Unit::Mole:
                return "Mole";
            case Unit::Candela:
                return "Candela";
            case Unit::Radian:
                return "Radian";
            case Unit::Steradian:
                return "Steradian";
            case Unit::MeterSquared:
                return "MeterSquared";
            case Unit::MeterCubed:
                return "MeterCubed";
            case Unit::MeterPerSecond:
                return "MeterPerSecond";
            case Unit::MeterPerSecondSquared:
                return "MeterPerSecondSquared";
            case Unit::RadianPerSecond:
                return "RadianPerSecond";
            case Unit::RadianPerSecondSquared:
                return "RadianPerSecondSquared";
            case Unit::Hertz:
                return "Hertz";
            case Unit::Joule:
                return "Joule";
            case Unit::Watt:
                return "Watt";
            case Unit::Pascal:
                return "Pascal";
            case Unit::Lumen:
                return "Lumen";
            case Unit::Lux:
                return "Lux";
            case Unit::Coulomb:
                return "Coulomb";
            case Unit::Ohm:
                return "Ohm";
            case Unit::Farad:
                return "Farad";
            case Unit::Weber:
                return "Weber";
            case Unit::Tesla:
                return "Tesla";
            case Unit::Henry:
                return "Henry";
            case Unit::Siemens:
                return "Siemens";
            case Unit::Becquerel:
                return "Becquerel";
            case Unit::Gray:
                return "Gray";
            case Unit::Sievert:
                return "Sievert";
            case Unit::Katal:
                return "Katal";

            case Unit::Unknown:
            default:
                return "Unknown";
        }
    }
#ifdef WIN32
    bool Hit()
    {
        bool hit = false;
        while( _kbhit() )
        {
            getchar();
            hit = true;
        }
        return hit;
    }
#endif
}


//=======================Global Info and Flags=======================
const char* HOSTADDR= "192.168.1.124";
const char* SERVERPORT = "4000";
std::string DroneIP = "192.168.1.10";
//std::string VICONHost = "192.17.178.232:801";//port always is 801
std::string VICONHost = "192.168.1.125:801";//port always is 801
std::string LogFile = "";
std::string MulticastAddress = "244.0.0.0:44801";
std::string wayPoints = "";
bool ConnectToMultiCast = false;
bool EnableMultiCast = false;


int main(int argc, char *argv[])
{
    //initialize the socket
    int sockfd;
    struct addrinfo hints, *servinfo, *p;
    int rv;
    int numbytes;
    char msg[256]="message";
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_DGRAM;
    if ((rv = getaddrinfo(HOSTADDR, SERVERPORT, &hints, &servinfo)) != 0) {
        std::cout<<"ERROR:"<<"getaddrinfo: "<<strerror(rv)<<std::endl;
        return 1;
    }
    // loop through all the results and make a socket
    for(p = servinfo; p != NULL; p = p->ai_next) {
        if ((sockfd = socket(p->ai_family, p->ai_socktype,
                        p->ai_protocol)) == -1) {
            perror("talker: socket");
            continue; 
        }
        break;
    }
    if (p == NULL) {
        std::cout<<"talker: failed to create socket"<<std::endl;
        return 2;
    }
    else{
        std::cout<<"talker: socket create SUCCESS"<<std::endl;
    }

    Client MyClient;
    //initialize VICON======================================================
    std::cout << "Connecting to " << VICONHost << " ..." << std::flush;
    while( !MyClient.IsConnected().Connected )
    {
        bool ok = false;
        if(ConnectToMultiCast)
        {
            // Multicast connection
            ok = ( MyClient.ConnectToMulticast( VICONHost , MulticastAddress ).Result 
                    == Result::Success );
        }
        else
        {
            ok =( MyClient.Connect( VICONHost ).Result == Result::Success );
        }
        if(!ok)
            std::cout << "Warning - connect failed... Retry." << std::endl;
        usleep(200);
        std::cout << std::endl;
    }
    // Enable some different data types
    MyClient.EnableSegmentData();
    MyClient.EnableMarkerData();
    MyClient.EnableUnlabeledMarkerData();
    MyClient.EnableDeviceData();
    std::cout << "Segment Data Enabled: "
        << Adapt( MyClient.IsSegmentDataEnabled().Enabled )<< std::endl;
    std::cout << "Marker Data Enabled: "
        << Adapt( MyClient.IsMarkerDataEnabled().Enabled )<< std::endl;
    std::cout << "Unlabeled Marker Data Enabled: "
        << Adapt( MyClient.IsUnlabeledMarkerDataEnabled().Enabled )<< std::endl;
    std::cout << "Device Data Enabled: "
        << Adapt( MyClient.IsDeviceDataEnabled().Enabled )<< std::endl;
    // Set the streaming mode
    MyClient.SetStreamMode( ViconDataStreamSDK::CPP::StreamMode::ClientPull );
    // Set the global up axis
    MyClient.SetAxisMapping( Direction::Forward, Direction::Left,
            Direction::Up ); // Z-up

    Output_GetVersion _Output_GetVersion = MyClient.GetVersion();
    std::cout << "Version: " << _Output_GetVersion.Major << "." 
        << _Output_GetVersion.Minor << "." 
        << _Output_GetVersion.Point << std::endl;

    //geting and send information==============================================
    static unsigned int msgCounter = 0;
    while(true)
    {
        while( !MyClient.IsConnected().Connected )
        {
            bool ok = false;
            if(ConnectToMultiCast)
            {
                // Multicast connection
                ok = ( MyClient.ConnectToMulticast( VICONHost , MulticastAddress ).Result 
                        == Result::Success );
            }
            else
            {
                ok =( MyClient.Connect( VICONHost ).Result == Result::Success );
            }
            if(!ok)
                std::cout << "Warning - connect failed..." << std::endl;
            std::cout << ".";
            usleep(200);
            std::cout << std::endl;
        }
        // Get a frame
        output_stream << "Waiting for new frame...";
        while( MyClient.GetFrame().Result != Result::Success )
        {
            // Sleep a little so that we don't lumber the CPU with a busy poll
#ifdef WIN32
            Sleep( 200 );
#else
            usleep( 200);
#endif
            output_stream << ".";
        }
        output_stream << std::endl;
        output_stream << "Latency: " << MyClient.GetLatencyTotal().Total << "s" << std::endl;
        unsigned int SubjectCount = MyClient.GetSubjectCount().SubjectCount;
        output_stream << "Subjects (" << SubjectCount << "):" << std::endl;
        std::string outToStarL=""; //the final output string to the StarL UDP port
        for( unsigned int SubjectIndex = 0 ; SubjectIndex < SubjectCount ; ++SubjectIndex )
        {
            output_stream << "  Subject #" << SubjectIndex << std::endl;
            // Get the subject name
            std::string SubjectName = MyClient.GetSubjectName( SubjectIndex ).SubjectName;
            output_stream << "    Name: " << SubjectName << std::endl;
            // Get the root segment
            std::string RootSegment = MyClient.GetSubjectRootSegmentName( SubjectName ).SegmentName;
            output_stream << "    Root Segment: " << RootSegment << std::endl;
            // Count the number of segments
            unsigned int SegmentCount = MyClient.GetSegmentCount( SubjectName ).SegmentCount;
            output_stream << "    Segments (" << SegmentCount << "):" << std::endl;

            for( unsigned int SegmentIndex = 0 ; SegmentIndex < SegmentCount ; ++SegmentIndex )
            {
                output_stream << "      Segment #" << SegmentIndex << std::endl;
                // Get the segment name
                std::string SegmentName = MyClient.GetSegmentName( SubjectName, SegmentIndex ).SegmentName;
                output_stream << "        Name: " << SegmentName << std::endl;
                outToStarL = "%|" + SegmentName + "|"; 

                // Get the global segment translation
                Output_GetSegmentGlobalTranslation _Output_GetSegmentGlobalTranslation = 
                    MyClient.GetSegmentGlobalTranslation( SubjectName, SegmentName );
                output_stream << "        Global Translation: (" << _Output_GetSegmentGlobalTranslation.Translation[ 0 ]  << ", " 
                    << _Output_GetSegmentGlobalTranslation.Translation[ 1 ]  << ", " 
                    << _Output_GetSegmentGlobalTranslation.Translation[ 2 ]  << ") " 
                    << Adapt( _Output_GetSegmentGlobalTranslation.Occluded ) << std::endl;
                outToStarL += ( std::to_string((int)_Output_GetSegmentGlobalTranslation.Translation[ 0 ]) + "|" +
                        std::to_string((int)_Output_GetSegmentGlobalTranslation.Translation[ 1 ]) + "|" +
                        std::to_string((int)_Output_GetSegmentGlobalTranslation.Translation[ 2 ]) + "|");
                //until now, "%|name|x|y|z|"


                // Get the global segment rotation in helical co-ordinates
                Output_GetSegmentGlobalRotationHelical _Output_GetSegmentGlobalRotationHelical = 
                    MyClient.GetSegmentGlobalRotationHelical( SubjectName, SegmentName );
                output_stream << "        Global Rotation Helical: (" << _Output_GetSegmentGlobalRotationHelical.Rotation[ 0 ]     << ", " 
                    << _Output_GetSegmentGlobalRotationHelical.Rotation[ 1 ]     << ", " 
                    << _Output_GetSegmentGlobalRotationHelical.Rotation[ 2 ]     << ") " 
                    << Adapt( _Output_GetSegmentGlobalRotationHelical.Occluded ) << std::endl;
                outToStarL += ( std::to_string((int)_Output_GetSegmentGlobalRotationHelical.Rotation[ 0 ]) + "|" +
                        std::to_string((int)_Output_GetSegmentGlobalRotationHelical.Rotation[ 1 ]) + "|" +
                        std::to_string((int)_Output_GetSegmentGlobalRotationHelical.Rotation[ 2 ]) + "|");
                //until now, "%|name|x|y|z|angle1|angle2|angle3|"
                outToStarL += DroneIP + "|";
                //until now, "%|name|x|y|z|angle1|angle2|angle3|xx.xx.xx.xx|"
                outToStarL += "&\n";
            }
            std::cout<<"the msg to StarL:"<<outToStarL;
        }
        //socket interface toward the phone(StarL)========================
        if ((numbytes = sendto(sockfd, outToStarL.c_str(), outToStarL.size(), 0,
                        p->ai_addr, p->ai_addrlen)) == -1) {
            perror("talker: sendto");
        }
        std::cout<<"talker: sent "<<numbytes<<"bytes to "<<HOSTADDR<<":"<<SERVERPORT<<std::endl;
        usleep(10*1000);
    }
    close(sockfd);
    return 0;
}
