#pragma once
#include <JuceHeader.h>

class NewProjectAudioProcessor : public juce::AudioProcessor
{
public:
    NewProjectAudioProcessor();
    ~NewProjectAudioProcessor() override;

    void prepareToPlay(double sampleRate, int samplesPerBlock) override;
    void releaseResources() override;
    void processBlock(juce::AudioBuffer<float>&, juce::MidiBuffer&) override;

    juce::AudioProcessorEditor* createEditor() override;
    bool hasEditor() const override { return true; }

    // State Management (Best Practice for Automation)
    static juce::AudioProcessorValueTreeState::ParameterLayout createParameterLayout();
    juce::AudioProcessorValueTreeState apvts{ *this, nullptr, "Parameters", createParameterLayout() };

    // Standard JUCE boilerplate omitted for brevity...
    const juce::String getName() const override { return "Acid Saturator"; }
    bool acceptsMidi() const override { return false; }
    bool producesMidi() const override { return false; }
    double getTailLengthSeconds() const override { return 0.0; }
    int getNumPrograms() override { return 1; }
    int getCurrentProgram() override { return 0; }
    void setCurrentProgram(int index) override {}
    const juce::String getProgramName(int index) override { return {}; }
    void changeProgramName(int index, const juce::String& newName) override {}
    void getStateInformation(juce::MemoryBlock& destData) override;
    void setStateInformation(const void* data, int sizeInBytes) override;
    float getLevel(int channel) const { return rmsLevels[channel].load(); }

private:
    // DSP Objects
    juce::dsp::Oversampling<float> oversampler;
    std::array<std::atomic<float>, 2> rmsLevels;
    // Smoothing for parameters to prevent "clicks" during automation
    juce::LinearSmoothedValue<float> driveSmoother;
    juce::LinearSmoothedValue<float> mixSmoother;
    juce::LinearSmoothedValue<float> outputSmoother;


    JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR(NewProjectAudioProcessor)
};