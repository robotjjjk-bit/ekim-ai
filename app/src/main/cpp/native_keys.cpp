#include <iostream>
#include <vector>
#include <string>
#include <cstdint>
#include <jni.h>

using namespace std;

class Key
{
private:
    uint8_t XOR;
    uint8_t XOR_2;
    vector<uint8_t> raw;
    string data;

    uint8_t mixKey(uint8_t base, int index)
    {
        return base ^ ((index * 31) & 0xFF);
    }

public:
    Key(const vector<uint8_t> raw, const uint8_t XOR1, const uint8_t XOR2, const string data)
    {
        this->data = data;
        this->raw = raw;
        this->XOR = XOR1;
        this->XOR_2 = XOR2;
    }

    vector<uint8_t> encrypt() //For getting the raw vector<uint8_t> to pass in Key();
    {
        vector<uint8_t> current;
        vector<uint8_t> result;

        int idx = 0;
        for(auto c: data)
        {
            current.push_back(c^mixKey(XOR, idx++));
        }
        for(uint8_t i: current)
        {
            result.push_back(i^mixKey(XOR_2, idx++));
        }
        return result;
    }

    string decrypt()
    {
        vector<uint8_t> current;
        string result = "";

        int idx = 0;
        for(uint8_t i: raw)
        {
            current.push_back(i^mixKey(XOR_2, idx++));
        }
        for(uint8_t j: current)
        {
            result += static_cast<char>(j^mixKey(XOR, idx++));
        }
        return result;
    }
};

void printHex(const vector<uint8_t>& raw) // to print your raw_groq vector<uint8_t>
{
    cout << "vector<uint8_t> OBFUSCATED = {\n    ";

    for (size_t i = 0; i < raw.size(); i++)
    {
        cout << "0x"
             << uppercase << hex
             << (int)raw[i];

        if (i != raw.size() - 1) cout << ", ";
        if ((i + 1) % 10 == 0) cout << "\n    ";
    }
    cout << "\n};\n";
}

const uint8_t GROQ_1 = 0x00000; // Your GROQ_KEY_1
const uint8_t GROQ_2 = 0x00000; // Your GROQ_KEY_2
const uint8_t GOOGLE_1 = 0x00000; // Your GOOGLE_KEY_1
const uint8_t GOOGLE_2 = 0x00000; // Your GOOGLE_KEY_2

vector<uint8_t> raw_groq = {
        0xDA, 0x9A, 0x52, 0xB7, 0x00, 0x92, 0x97, 0x95, 0x93, 0xB8,
        0x89, 0x3b, 0xF9, 0x8F, 0x52, 0xBA, 0xE4, 0xE0, 0x86, 0x97,
        0x92, 0xC6, 0x87, 0xC2, 0xfD
};

vector<uint8_t> raw_groq_1 = {
        0xC5, 0x6A, 0x7F, 0xCB, 0x9C, 0x41, 0x4C, 0xDD, 0xDB, 0x95,
        0x72, 0xC6, 0xFB, 0x4A, 0x65, 0xF2, 0x00, 0x45, 0x45, 0xC3,
        0x6F, 0x72, 0x51, 0xC8, 0xff, 0x68, 0x71, 0xE8, 0xCE, 0x45,
        0x7B
};

vector<uint8_t> raw_google = {
        0x31, 0xFF, 0xEA, 0xF1, 0xC3, 0xE9, 0xD3, 0x00, 0xD2, 0xA9,
        0xE1, 0x78, 0xA9, 0xF8, 0xC3, 0xD9
};

vector<uint8_t> raw_google_1 = {
        0x7A, 0x93, 0x8C, 0x5, 0x3E, 0x98, 0x86, 0x16, 0x6A, 0x91,
        0x64, 0x1C, 0xDA, 0xC8, 0xFC, 0x35, 0x8C, 0x91, 0xDE, 0x15,
        0xE8, 0x8, 0x18
};

const string GROQ = "_GROQ_API_KEY_";
const string GOOGLE = "_GOOGLE_API_KEY_";

Key KEY_GROQ_FIRST = Key(raw_groq, GROQ_1, GROQ_2, GROQ);
Key KEY_GROQ_SECOND = Key(raw_groq_1, GROQ_1, GROQ_2, GROQ);
Key KEY_GOOGLE_FIRST = Key(raw_google, GOOGLE_1, GOOGLE_2, GOOGLE);
Key KEY_GOOGLE_SECOND = Key(raw_google_1, GOOGLE_1, GOOGLE_2, GOOGLE);

extern "C"
JNIEXPORT jstring JNICALL
Java_net_ekmai_android_in_utilities_NativeManager_getGroqNative(JNIEnv *env, jobject thiz) {
    string k = KEY_GROQ_FIRST.decrypt();
    string l = KEY_GROQ_SECOND.decrypt();
    return env->NewStringUTF((k+l).c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_net_ekmai_android_in_utilities_NativeManager_getGeminiNative(JNIEnv *env, jobject thiz) {
    string k = KEY_GOOGLE_FIRST.decrypt();
    string l = KEY_GOOGLE_SECOND.decrypt();
    return env->NewStringUTF((k+l).c_str());
}