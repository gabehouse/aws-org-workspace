#pragma once
#include <JuceHeader.h>
#include "PluginProcessor.h"

// Custom LookAndFeel for that "Signature" Portfolio look
class AcidLookAndFeel : public juce::LookAndFeel_V4 {
public:
    AcidLookAndFeel() {
        setColour(juce::Slider::thumbColourId, juce::Colours::springgreen);
        setColour(juce::Slider::rotarySliderFillColourId, juce::Colours::springgreen.withAlpha(0.6f));
        setColour(juce::Slider::textBoxOutlineColourId, juce::Colours::transparentBlack);
    }
};

class NewProjectAudioProcessorEditor : public juce::AudioProcessorEditor,
    private juce::Timer // Inheriting Timer for the LED refresh
{
public:
    NewProjectAudioProcessorEditor(NewProjectAudioProcessor&);
    ~NewProjectAudioProcessorEditor() override;

    void paint(juce::Graphics&) override;
    void resized() override;

    // Timer callback to refresh the UI (LED)
    void timerCallback() override;

    // Helper to snap parameters to preset values
    void loadPreset(float drive, float mix, float output);

private:
    AcidLookAndFeel customLNF;
    float linkOffset = 0.0f;
    // UI Components
    juce::Slider driveSlider, mixSlider, outputSlider;
    juce::Label driveLabel, mixLabel, outputLabel;

    juce::ComboBox presetMenu;
    juce::ToggleButton makeupButton; // The "Auto-Makeup" toggle

    // Attachments (Best Practice: Using APVTS to keep Audio & UI in sync)
    using SliderAttachment = juce::AudioProcessorValueTreeState::SliderAttachment;
    using ButtonAttachment = juce::AudioProcessorValueTreeState::ButtonAttachment;

    std::unique_ptr<SliderAttachment> driveAttachment;
    std::unique_ptr<SliderAttachment> mixAttachment;
    std::unique_ptr<SliderAttachment> outputAttachment;
    std::unique_ptr<ButtonAttachment> makeupAttachment;

    NewProjectAudioProcessor& audioProcessor;

    JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR(NewProjectAudioProcessorEditor)
};