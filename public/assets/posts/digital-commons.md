# Digital commons and the civic stack

A civic-ops perspective on governing shared digital infrastructure that citizens depend on daily.

## Civic-grade infrastructure

Public systems should read like clean source files—transparent and testable with clear interfaces.

## Procurement as code review

- Ship RFPs with reproducible test cases.
- Reward vendors that keep interfaces boring.
- Make performance guarantees measurable and automatable.

## Trust budgets

The trust budget drops every time the interface surprises the public. Ship predictable defaults and reduce surprise pathways.

```bash
$ ls && echo "hi"
```

```python
text = input('Type a number, and its factorial will be printed: ')
n = int(text)

if n < 0:
    raise ValueError('You must enter a non-negative integer')

factorial = 1
for i in range(2, n + 1):
    factorial *= i

print(factorial)
```

